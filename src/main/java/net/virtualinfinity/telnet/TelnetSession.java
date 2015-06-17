package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.OutputBuffer;
import net.virtualinfinity.telnet.option.handlers.OptionReceiver;
import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;
import net.virtualinfinity.telnet.option.handlers.SubNegotiationReceiver;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class TelnetSession {
    private static final Logger logger = Logger.getLogger(TelnetSession.class);
    private final OutputBuffer output;
    private final Map<Integer, OptionSessionHandler<?>> optionSessionHandler = new HashMap<>();
    private final OptionState[] optionStates = new OptionState[256];
    private final TelnetSessionListener listener;
    private TelnetCommandState commandState = TelnetCommandState.initial();
    private boolean shutdown;
    private boolean shuttingDown;

    public TelnetSession(OutputBuffer outputBuffer, TelnetSessionListener listener) {
        this.output = outputBuffer;
        this.listener = listener;
    }

    public TelnetDataReceiver createTelnetDataReceiver() {
        return new TelnetDataReceiver(new CommandReceiver());
    }


    private void enabledLocally(int optionId) {
        dispatchToRemoteHandler(optionId, OptionSessionHandler::enabledLocally);
    }
    private void disabledLocally(int optionId) {
        dispatchToRemoteHandler(optionId, OptionSessionHandler::disabledLocally);
    }

    private void dispatchToRemoteHandler(int optionId, BiConsumer<OptionSessionHandler<?>, TelnetSession> command) {
        final OptionSessionHandler<?> handler = optionSessionHandler.get(optionId);
        if (handler != null) {
            command.accept(handler, this);
        }
    }

    private void enabledRemotely(int optionId) {
        dispatchToRemoteHandler(optionId, OptionSessionHandler::enabledRemotely);
    }

    private void disabledRemotely(int optionId) {
        dispatchToRemoteHandler(optionId, OptionSessionHandler::disabledRemotely);
    }

    public boolean isShutdown() {
        return shutdown;
    }

    private void sendDo(int optionId) {
        logOptionCommand("Sent DO", optionId);
        sendOptionCommand(TelnetConstants.DO, (byte) optionId);
    }

    private void sendDont(int optionId) {
        logOptionCommand("Sent DON'T", optionId);
        sendOptionCommand(TelnetConstants.DONT, (byte) optionId);
    }

    private void sendWill(int optionId) {
        logOptionCommand("Sent WILL", optionId);
        sendOptionCommand(TelnetConstants.WILL, (byte) optionId);
    }

    private void sendWont(int optionId) {
        logOptionCommand("Sent WONT", optionId);
        sendOptionCommand(TelnetConstants.WONT, (byte) optionId);
    }

    private void sendOptionCommand(byte command, byte optionId) {
        rawWrite(ByteBuffer.wrap(new byte[]{TelnetConstants.IAC, command, optionId}));
    }

    public void processedData(ByteBuffer data) {
        listener.processData(data, this);
    }

    public void sendSubNegotiation(int optionId, ByteBuffer data) {
        sendOptionCommand(TelnetConstants.SB, (byte)optionId);
        rawWrite(data);
        rawWrite(ByteBuffer.wrap(new byte[]{TelnetConstants.IAC, TelnetConstants.SE}));
    }

    public void send(ByteBuffer inputData) {
        ByteBuffer sliced = inputData.slice();
        while (sliced.hasRemaining()) {
            if (sliced.get() == TelnetConstants.IAC) {
                sliced.flip();
                rawWrite(sliced);
                sliced.position(sliced.position() - 1);
                sliced = sliced.slice();
                sliced.position(1);
            }
        }
        sliced.flip();
        rawWrite(sliced);
    }

    public void send(byte[] data) {
        send(ByteBuffer.wrap(data));
    }

    public void close() {
        shuttingDown = true;
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

    private void logOptionCommand(String command, int optionId) {
        if (logger.isDebugEnabled()) {
            final Option option = Option.byId(optionId);
            logger.debug(command + ": " + option + " (" + optionId + ")");
        }
    }

    private void rawWrite(ByteBuffer data) {
        output.append(data);
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

    private class CommandReceiver implements TelnetCommandReceiver {
        private final TelnetSession telnetSession = TelnetSession.this;

        public CommandReceiver() {
        }

        @Override
        public void receivedBreak() {
            listener.doBreak(telnetSession);
        }

        @Override
        public void receivedInterrupt() {
            listener.doInterrupt(telnetSession);
        }

        @Override
        public void receivedAbortOutput() {
            listener.doAbortOutput(telnetSession);
        }

        @Override
        public void receivedAreYouThere() {
            listener.doAreYouThere(telnetSession);
        }

        @Override
        public void receivedEraseCharacter() {
            listener.doEraseCharacter(telnetSession);
        }

        @Override
        public void receivedEraseLine() {
            listener.doEraseLine(telnetSession);

        }

        @Override
        public void receivedGoAhead() {
            listener.doGoAhead(telnetSession);
        }

        @Override
        public void receivedEndSubNegotiation() {
            commandState = commandState.endSubNegotiation(telnetSession);
        }

        @Override
        public void receivedData(ByteBuffer bytes) {
            commandState = commandState.data(bytes, telnetSession);
        }

        @Override
        public void receivedIAC() {
            commandState = commandState.data(ByteBuffer.wrap(new byte[]{TelnetConstants.IAC}), TelnetSession.this);
        }

        @Override
        public void receivedStartSubNegotiation(int optionId) {
            final OptionSessionHandler<?> handler = optionSessionHandler.get(optionId);
            commandState = TelnetCommandState.subNegotiating(handler);
            if (handler != null) {
                handler.startSubNegotiation(telnetSession);
            }
        }

        @Override
        public void receivedDo(int optionId) {
            logOptionCommand("Received DO", optionId);
            optionState(optionId).receivedDo().accept(telnetSession, optionId);
        }

        @Override
        public void receivedDont(int optionId) {
            logOptionCommand("Received DON'T", optionId);
            optionState(optionId).receivedDont().accept(telnetSession, optionId);
        }

        @Override
        public void receivedWill(int optionId) {
            logOptionCommand("Received WILL", optionId);
            optionState(optionId).receivedWill().accept(telnetSession, optionId);
        }

        @Override
        public void receivedWont(int optionId) {
            logOptionCommand("Received WON'T", optionId);
            optionState(optionId).receivedWont().accept(telnetSession, optionId);
        }

    }
}
