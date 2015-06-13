package net.virtualinfinity.telnet;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.ObjIntConsumer;

import static net.virtualinfinity.telnet.TelnetConstants.*;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public abstract class TelnetStreamState {
    private static final TelnetStreamState NORMAL_STATE = new NormalState();
    private static final TelnetStreamState IN_IAC = new InIAC();

    public abstract TelnetStreamState accept(ByteBuffer buffer, TelnetCommandReceiver session) throws IOException;

    public static TelnetStreamState initial() {
        return NORMAL_STATE;
    }

    private static class InIAC extends TelnetStreamState {
        private static final WaitingForOption IN_DO = new WaitingForOption(TelnetCommandReceiver::receivedDo);
        private static final WaitingForOption IN_DONT = new WaitingForOption(TelnetCommandReceiver::receivedDont);
        private static final WaitingForOption IN_WILL = new WaitingForOption(TelnetCommandReceiver::receivedWill);
        private static final WaitingForOption IN_WONT = new WaitingForOption(TelnetCommandReceiver::receivedWont);
        private static final WaitingForOption IN_SB = new WaitingForOption(TelnetCommandReceiver::startSubNegotiation);

        @Override
        public TelnetStreamState accept(ByteBuffer buffer, TelnetCommandReceiver commandReceiver) throws IOException {
            final byte command = buffer.get();
            switch (command) {
                case IAC:
                    commandReceiver.receivedIAC();
                case NOP:
                    return NORMAL_STATE;
                case DO:
                    return IN_DO;
                case DONT:
                    return IN_DONT;
                case WILL:
                    return IN_WILL;
                case WONT:
                    return IN_WONT;
                case SB:
                    return IN_SB;
                case SE:
                    commandReceiver.endSubNegotiation();
                    return NORMAL_STATE;
                case BRK:
                    commandReceiver.receivedBreak();
                    return NORMAL_STATE;
                case IP:
                    commandReceiver.receivedInterrupt();
                    return NORMAL_STATE;
                case AO:
                    commandReceiver.receivedAbortOutput();
                    return NORMAL_STATE;
                case AYT:
                    commandReceiver.receivedAreYouThere();
                    return NORMAL_STATE;
                case EC:
                    commandReceiver.receivedEraseCharacter();
                    return NORMAL_STATE;
                case EL:
                    commandReceiver.receiveEraseLine();
                    return NORMAL_STATE;
                case GA:
                    commandReceiver.receivedGoAhead();
                    return NORMAL_STATE;

            }
            Logger.getLogger(TelnetStreamState.class).error("Unexpected command after IAC: " + (((int)command) & 255));
            return NORMAL_STATE;
        }

    }

    private static class WaitingForOption extends TelnetStreamState {
        private final ObjIntConsumer<TelnetCommandReceiver> sessionCommand;

        public WaitingForOption(ObjIntConsumer<TelnetCommandReceiver> sessionCommand) {
            this.sessionCommand = sessionCommand;
        }

        @Override
        public TelnetStreamState accept(ByteBuffer buffer, TelnetCommandReceiver session) {
            sessionCommand.accept(session, ((int) buffer.get())&0xFF);

            return NORMAL_STATE;
        }
    }

    private static class NormalState extends TelnetStreamState {
        @Override
        public TelnetStreamState accept(ByteBuffer buffer, TelnetCommandReceiver session) throws IOException {
            final ByteBuffer sliced = buffer.slice();
            while (sliced.hasRemaining()) {
                if (sliced.get() == IAC) {
                    final int count = sliced.position();
                    buffer.position(buffer.position() + count);
                    sliced.rewind();
                    sliced.limit(count-1);
                    session.receivedData(sliced);

                    return IN_IAC;
                }
            }
            session.receivedData(buffer);

            return this;
        }
    }
}
