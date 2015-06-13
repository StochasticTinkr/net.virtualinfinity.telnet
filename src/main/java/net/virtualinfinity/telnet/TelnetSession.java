package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.SelectionKeyActions;
import net.virtualinfinity.telnet.option.handlers.OptionReceiver;
import net.virtualinfinity.telnet.option.handlers.SubNegotiationReceiver;
import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;

import static net.virtualinfinity.telnet.TelnetConstants.*;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class TelnetSession implements TelnetCommandReceiver, SelectionKeyActions {
    private static final int INPUT_BUFFER_SIZE = 256;
    private static final Logger logger = Logger.getLogger(TelnetSession.class);
    private final SocketChannel channel;
    private final ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
    private final Queue<ByteBuffer> output = new LinkedList<>();
    private final Map<Integer, OptionSessionHandler<?>> optionSessionHandler = new HashMap<>();
    private final OptionState[] optionStates = new OptionState[256];
    private final TelnetSessionListener listener;
    private SelectionKey selectionKey;

    private TelnetStreamState streamState = TelnetStreamState.initial();
    private TelnetCommandState commandState = TelnetCommandState.initial();

    public TelnetSession(SocketChannel channel, TelnetSessionListener listener) {
        this.channel = channel;
        this.listener = listener;
    }

    private void enabledLocally(int optionId) {
        // TODO:
    }
    private void disabledLocally(int optionId) {
        // TODO:
    }

    private void dispatchToRemoteHandler(int optionId, BiConsumer<OptionSessionHandler<?>, TelnetSession> command) {
        final OptionSessionHandler<?> handler = optionSessionHandler.get(optionId);
        if (handler != null) {
            command.accept(handler, this);
        }
    }
    private void enabledRemotely(int optionId) {
        dispatchToRemoteHandler(optionId, OptionSessionHandler::enabledLocally);
    }

    private void disabledRemotely(int optionId) {
        dispatchToRemoteHandler(optionId, OptionSessionHandler::disabledLocally);
    }

    public void selected() throws IOException {
        trimOutputBuffer();
        selectionKey.interestOps(interestOps());
        if (!output.isEmpty()) {
            if (isShutdown()) {
                channel.close();
                return;
            }
            if (selectionKey.isWritable()) {
                final long start = System.nanoTime();
                while (!output.isEmpty() && channel.write(output.peek()) > 0) {
                    trimOutputBuffer();
                    if (isShutdown()) {
                        channel.close();
                        return;
                    }
                    if (TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS) > 10) {
                        break;
                    }
                }
            }
            // Don't read more until the output buffer is empty.
            // This is very important to prevent malicious clients from DoS by spamming without reading.
            if (!output.isEmpty()) {
                return;
            }
        }
        if (selectionKey.isReadable()) {
            channel.read(inputBuffer);
            inputBuffer.flip();
            try {
                while (inputBuffer.hasRemaining()) {
                    streamState = streamState.accept(inputBuffer, this);
                }
            } finally {
                inputBuffer.compact();
            }
        }
        selectionKey.interestOps(interestOps());
    }

    public boolean isShutdown() {
        return !output.isEmpty() && output.peek() == null;
    }

    private void trimOutputBuffer() {
        while (!output.isEmpty() && output.peek() != null && !output.peek().hasRemaining()) {
            output.remove();
        }
    }

    public void receivedData(ByteBuffer bytes) throws IOException {
        commandState = commandState.data(bytes, this);
    }

    @Override
    public void receivedBreak() {
        listener.doBreak(this);
    }

    @Override
    public void receivedInterrupt() {
        listener.doInterrupt(this);
    }

    @Override
    public void receivedAbortOutput() {
        listener.doAbortOutput(this);
    }

    @Override
    public void receivedAreYouThere() {
        listener.doAreYouThere(this);
    }

    @Override
    public void receivedEraseCharacter() {
        listener.doEraseCharacter(this);
    }

    @Override
    public void receiveEraseLine() {
        listener.doEraseLine(this);

    }

    @Override
    public void receivedGoAhead() {
        listener.doGoAhead(this);
    }

    public void data(byte b) throws IOException {
        commandState = commandState.data(b, this);
    }

    public void endSubNegotiation() {
        commandState = commandState.endSubNegotiation(this);
    }

    @Override
    public void receivedIAC() throws IOException {
        data(IAC);
    }

    public void startSubNegotiation(int optionId) {
        final OptionSessionHandler<?> handler = optionSessionHandler.get(optionId);
        commandState = TelnetCommandState.subNegotiating(handler);
        if (handler != null) {
            handler.startSubNegotiation(this);
        }
    }

    private void logOptionCommand(String command, int optionId) {
        if (logger.isDebugEnabled()) {
            final Option option = Option.byId(optionId);
            logger.debug(command + ": " + option + " (" + optionId + ")");
        }
    }

    @Override
    public void receivedDo(int optionId) {
        logOptionCommand("Received DO", optionId);
        optionState(optionId).receivedDo().accept(this, optionId);
    }

    @Override
    public void receivedDont(int optionId) {
        logOptionCommand("Received DON'T", optionId);
        optionState(optionId).receivedDont().accept(this, optionId);
    }

    @Override
    public void receivedWill(int optionId) {
        logOptionCommand("Received WILL", optionId);
        optionState(optionId).receivedWill().accept(this, optionId);
    }

    @Override
    public void receivedWont(int optionId) {
        logOptionCommand("Received WON'T", optionId);
        optionState(optionId).receivedWont().accept(this, optionId);
    }

    private void sendDo(int optionId) {
        logOptionCommand("Sent DO", optionId);
        sendOptionCommand(DO, (byte) optionId);
    }

    private void sendDont(int optionId) {
        logOptionCommand("Sent DON'T", optionId);
        sendOptionCommand(DONT, (byte) optionId);
    }

    private void sendWill(int optionId) {
        logOptionCommand("Sent WILL", optionId);
        sendOptionCommand(WILL, (byte) optionId);
    }

    private void sendWont(int optionId) {
        logOptionCommand("Sent WONT", optionId);
        sendOptionCommand(WONT, (byte) optionId);
    }

    private void sendOptionCommand(byte command, byte optionId) {
        output.add(ByteBuffer.wrap(new byte[]{IAC, command, optionId}));
    }

    public void processedData(ByteBuffer data) throws IOException {
        listener.processData(data, this);
    }

    public void sendSubNegotiation(int optionId, ByteBuffer data) {
        sendOptionCommand(SB, (byte)optionId);
        output.add(data);
        output.add(ByteBuffer.wrap(new byte[]{IAC, SE}));
    }

    public void writeData(ByteBuffer inputData) {
        final byte[] data =new byte[inputData.remaining()];
        inputData.get(data);
        writeData(data);

    }

    public void writeData(byte[] data) {
        int iacCount = 0;
        for (final byte datum : data) {
            if (datum == IAC) {
                ++iacCount;
            }
        }
        final ByteBuffer bb;
        if (iacCount != 0) {
            bb = ByteBuffer.allocate(iacCount + data.length);
            for (final byte datum : data) {
                if (datum == IAC) {
                    bb.put(IAC);
                }
                bb.put(datum);
            }
        } else {
            bb = ByteBuffer.wrap(Arrays.copyOf(data, data.length));
        }
        output.add(bb);
        if (selectionKey != null) {
            selectionKey.interestOps(interestOps());
        }
    }

    public void close() {
        output.add(null);
    }

    @Override
    public int interestOps() {
        if (output.peek() != null) {
            return SelectionKey.OP_READ | SelectionKey.OP_WRITE;
        } else {
            return SelectionKey.OP_READ;
        }
    }

    enum Response implements ObjIntConsumer<TelnetSession> {
        SEND_WONT(TelnetSession::sendWont),
        IS_ENABLED_LOCALLY(TelnetSession::enabledLocally),
        IS_DISABLED_LOCALLY(TelnetSession::disabledLocally),
        NO_RESPONSE((session, value) -> {}),
        SEND_DO(TelnetSession::sendDo),
        IS_ENABLED_REMOTELY(TelnetSession::enabledRemotely),
        IS_DISABLED_REMOTELY(TelnetSession::disabledRemotely),
        SEND_DONT(TelnetSession::sendDont),
        SEND_WILL(TelnetSession::sendWill),
        ;

        private final ObjIntConsumer<TelnetSession> responder;

        Response(ObjIntConsumer<TelnetSession> responder) {
            this.responder = responder;
        }

        public void accept(TelnetSession session, int optionId) {
            responder.accept(session, optionId);
        }

        public ObjIntConsumer<TelnetSession> and(ObjIntConsumer<TelnetSession> otherResponse) {
            return (session, value) -> {
                accept(session, value);
                otherResponse.accept(session, value);
            };
        }
    }

    private OptionState optionState(int optionId) {
        OptionState optionState = optionStates[optionId&255];
        if (optionState == null) {
            optionState = new OptionState();
            optionStates[optionId] = optionState;
        }
        return optionState;
    }

    public <T> OptionHandle installSubNegotiationReceiver(SubNegotiationReceiver<T> handler) {
        final int optionId = handler.optionCode();
        optionSessionHandler.put(optionId, new OptionSessionHandler<>(handler, null));
        return new OptionHandle(handler);
    }

    public <T> OptionHandle installOptionReceiver(OptionReceiver<T> handler) {
        final int optionId = handler.optionCode();
        optionSessionHandler.put(optionId, new OptionSessionHandler<>(null, handler));
        return new OptionHandle(handler);
    }

    public <T, R extends SubNegotiationReceiver<T>&OptionReceiver<T>> OptionHandle installOptionReceiver(R receiver) {
        final int optionId = receiver.optionCode();
        optionSessionHandler.put(optionId, OptionSessionHandler.of(receiver));
        return new OptionHandle(receiver);
    }


    private OptionState optionState(HasOptionCode option) {
        return optionState(option.optionCode());
    }

    public void allowRemote(HasOptionCode option) {
        optionState(option).allowRemote();
    }

    public void allowLocal(HasOptionCode option) {
        optionState(option).allowLocal();
    }

    public OptionHandle option(HasOptionCode hasOptionCode) {
        return new OptionHandle(hasOptionCode);
    }

    public OptionHandle option(int optionCode) {
        return option(() -> optionCode);
    }

    private void updateOptionState(HasOptionCode option, Function<OptionState, ObjIntConsumer<TelnetSession>> command) {
        command.apply(optionState(option.optionCode())).accept(this, option.optionCode());
    }

    private void requestRemoteEnable(HasOptionCode option) {
        updateOptionState(option, OptionState::enableRemote);
    }

    private void requestLocalEnable(HasOptionCode option) {
        updateOptionState(option, OptionState::enableLocal);
    }

    private void requestRemoteDisable(HasOptionCode option) {
        updateOptionState(option, OptionState::disableRemote);
    }

    private void requestLocalDisable(HasOptionCode option) {
        updateOptionState(option, OptionState::disableLocal);
    }

    @Override
    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public class OptionHandle {
        private final HasOptionCode option;

        private OptionHandle(HasOptionCode option) {
            this.option = option;
        }

        public OptionHandle requestRemoteEnable() {
            TelnetSession.this.requestRemoteEnable(option);

            return this;
        }

        public OptionHandle requestLocalEnable() {
            TelnetSession.this.requestLocalEnable(option);

            return this;
        }

        public OptionHandle requestRemoteDisable() {
            TelnetSession.this.requestRemoteDisable(option);

            return this;
        }

        public OptionHandle requestLocalDisable() {
            TelnetSession.this.requestLocalDisable(option);

            return this;
        }

        public OptionHandle allowLocal() {
            TelnetSession.this.allowLocal(option);

            return this;
        }

        public OptionHandle allowRemote() {
            TelnetSession.this.allowRemote(option);

            return this;
        }
    }

}