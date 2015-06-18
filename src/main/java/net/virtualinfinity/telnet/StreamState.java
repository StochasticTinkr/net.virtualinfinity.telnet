package net.virtualinfinity.telnet;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.ObjIntConsumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public abstract class StreamState {
    private static final StreamState NORMAL_STATE = new NormalState();
    private static final StreamState IN_IAC = new InIAC();

    public abstract StreamState accept(ByteBuffer buffer, CommandReceiver commandReceiver) throws IOException;

    public static StreamState initial() {
        return NORMAL_STATE;
    }

    private static class InIAC extends StreamState {
        private static final WaitingForOption IN_DO = new WaitingForOption(CommandReceiver::receivedDo);
        private static final WaitingForOption IN_DONT = new WaitingForOption(CommandReceiver::receivedDont);
        private static final WaitingForOption IN_WILL = new WaitingForOption(CommandReceiver::receivedWill);
        private static final WaitingForOption IN_WONT = new WaitingForOption(CommandReceiver::receivedWont);
        private static final WaitingForOption IN_SB = new WaitingForOption(CommandReceiver::receivedStartSubNegotiation);

        @Override
        public StreamState accept(ByteBuffer buffer, CommandReceiver commandReceiver) throws IOException {
            final byte command = buffer.get();
            switch (command) {
                case TelnetConstants.IAC:
                    commandReceiver.receivedIAC();
                case TelnetConstants.NOP:
                    return NORMAL_STATE;
                case TelnetConstants.DO:
                    return IN_DO;
                case TelnetConstants.DONT:
                    return IN_DONT;
                case TelnetConstants.WILL:
                    return IN_WILL;
                case TelnetConstants.WONT:
                    return IN_WONT;
                case TelnetConstants.SB:
                    return IN_SB;
                case TelnetConstants.SE:
                    commandReceiver.receivedEndSubNegotiation();
                    return NORMAL_STATE;
                case TelnetConstants.BRK:
                    commandReceiver.receivedBreak();
                    return NORMAL_STATE;
                case TelnetConstants.IP:
                    commandReceiver.receivedInterrupt();
                    return NORMAL_STATE;
                case TelnetConstants.AO:
                    commandReceiver.receivedAbortOutput();
                    return NORMAL_STATE;
                case TelnetConstants.AYT:
                    commandReceiver.receivedAreYouThere();
                    return NORMAL_STATE;
                case TelnetConstants.EC:
                    commandReceiver.receivedEraseCharacter();
                    return NORMAL_STATE;
                case TelnetConstants.EL:
                    commandReceiver.receivedEraseLine();
                    return NORMAL_STATE;
                case TelnetConstants.GA:
                    commandReceiver.receivedGoAhead();
                    return NORMAL_STATE;

            }
            Logger.getLogger(StreamState.class).error("Unexpected command after IAC: " + (((int)command) & 255));
            return NORMAL_STATE;
        }

    }

    private static class WaitingForOption extends StreamState {
        private final ObjIntConsumer<CommandReceiver> command;

        public WaitingForOption(ObjIntConsumer<CommandReceiver> command) {
            this.command = command;
        }

        @Override
        public StreamState accept(ByteBuffer buffer, CommandReceiver commandReceiver) {
            command.accept(commandReceiver, ((int) buffer.get())&0xFF);

            return NORMAL_STATE;
        }
    }

    private static class NormalState extends StreamState {
        @Override
        public StreamState accept(ByteBuffer buffer, CommandReceiver commandReceiver) throws IOException {
            final ByteBuffer sliced = buffer.slice();
            while (sliced.hasRemaining()) {
                if (sliced.get() == TelnetConstants.IAC) {
                    final int count = sliced.position();
                    buffer.position(buffer.position() + count);
                    sliced.rewind();
                    sliced.limit(count-1);
                    commandReceiver.receivedData(sliced);

                    return IN_IAC;
                }
            }
            commandReceiver.receivedData(buffer);
            // Ensure that the buffer has been consumed.
            buffer.position(buffer.limit());
            return this;
        }
    }
}
