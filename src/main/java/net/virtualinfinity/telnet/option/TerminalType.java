package net.virtualinfinity.telnet.option;

import net.virtualinfinity.nio.BufferUtils;
import net.virtualinfinity.telnet.Option;
import net.virtualinfinity.telnet.Session;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Handles the Terminal Type option negotiation.
 *
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class TerminalType extends AbstractNegotiatingOption {
    private static final byte TTYPE_SEND = 1;
    private static final byte TTYPE_IS = 0;
    private TerminalTypeSelector terminalTypeSelector = () -> Collections.singleton("dumb").iterator();
    private final Collection<RemoteTerminalTypeListener> remoteTerminalTypeListeners = new ArrayList<>();
    private Iterator<String> iterator;
    ByteBuffer output = ByteBuffer.allocate(41);

    private TerminalType(Session session) {
        super(session, Option.TERMINAL_TYPE);
    }

    /**
     * Attaches a new TerminalType option handler to the session.
     * @param session the session
     * @return a new TerminalType instance.
     */
    public static TerminalType of(Session session) {
        return create(TerminalType::new, session);
    }

    private void sendNext() {
        if (iterator == null) {
            iterator = terminalTypeSelector.iterator();
        }
        if (iterator.hasNext()) {
            final String next = iterator.next();
            final byte[] terminal = next.getBytes(Charset.forName("US-ASCII"));
            output.clear();
            output.put(TTYPE_IS);
            output.put(terminal, 0, Math.min(40, terminal.length));
            output.flip();
        } else {
            iterator = null;
        }
        sendSubNegotiation(output.asReadOnlyBuffer());
    }

    /**
     * Request that the remote sends its terminal type to us.
     *
     * @see #addRemoteTerminalTypeListener(RemoteTerminalTypeListener)
     * @return this.
     */
    public TerminalType requestRemoteEnable() {
        optionHandle.requestRemoteEnable();
        return this;
    }

    /**
     * Request the next terminal type from the remote, iff the option is enabled.
     * @see #addRemoteTerminalTypeListener(RemoteTerminalTypeListener)
     * @return this.
     */
    public TerminalType requestNext() {
        if (optionHandle.isEnabledRemotely()) {
            sendSubNegotiation(ByteBuffer.wrap(new byte[] {TTYPE_SEND}));
        }
        return this;
    }

    /**
     * Allow the remote to request our terminal types.
     *
     * @param terminalTypeSelector
     *
     * @return
     */
    public TerminalType allowLocal(TerminalTypeSelector terminalTypeSelector) {
        this.terminalTypeSelector = terminalTypeSelector;
        optionHandle.allowLocal();

        return this;
    }

    public TerminalType disallowLocal() {
        optionHandle.allowLocal();
        return this;
    }

    public TerminalType addRemoteTerminalTypeListener(RemoteTerminalTypeListener listener) {
        remoteTerminalTypeListeners.add(listener);
        return this;
    }

    public TerminalType removeRemoteTerminalTypeListener(RemoteTerminalTypeListener listener) {
        remoteTerminalTypeListeners.remove(listener);
        return this;
    }

    private void terminalTypeReported(String terminalType) {
        remoteTerminalTypeListeners.forEach(listener -> listener.terminalSet(terminalType));
    }

    @Override
    protected SubNegotiationListener getNegotiationListener() {
        return new MySubNegotiationListener(this);
    }

    @Override
    protected OptionStateListener getStateListener() {
        return null;
    }

    private static class MySubNegotiationListener implements SubNegotiationListener {
        private final TerminalType terminalType;
        private State state = State.INITIAL;

        public MySubNegotiationListener(TerminalType terminalType) {
            this.terminalType = terminalType;
        }

        private final ByteBuffer input = ByteBuffer.allocate(40);

        @Override
        public void startSubNegotiation() {
        }

        @Override
        public void subNegotiationData(ByteBuffer data) {
            state = state.data(data, this);
        }

        @Override
        public void endSubNegotiation() {
            state.finish(this);
        }

        enum State {
            INITIAL {

                @Override
                public State data(ByteBuffer data, MySubNegotiationListener snl) {
                    if (!data.hasRemaining()) {
                        return this;
                    }
                    switch (data.get()) {
                        case TTYPE_IS:
                            return IS.data(data, snl);
                        case TTYPE_SEND:
                            snl.terminalType.sendNext();
                            return SENT_NEXT.data(data, snl);
                    }
                    data.position(data.position()-1);
                    return ERROR.data(data, snl);
                }
            },
            IS {
                @Override
                public State data(ByteBuffer data, MySubNegotiationListener snl) {
                    BufferUtils.putWhatFits(snl.input, data);
                    return this;
                }

                @Override
                public void finish(MySubNegotiationListener snl) {
                    snl.input.flip();
                    final byte[] bytes = new byte[snl.input.remaining()];
                    snl.input.put(bytes);
                    snl.terminalType.terminalTypeReported(new String(bytes, Charset.forName("US-ASCII")));
                }
            },
            SENT_NEXT {
                @Override
                public State data(ByteBuffer data, MySubNegotiationListener snl) {
                    if (data.hasRemaining()) {
                        return ERROR;
                    } else {
                        return this;
                    }
                }
            },
            ERROR {
                @Override
                public State data(ByteBuffer data, MySubNegotiationListener snl) {
                    return this;
                }
            },;

            public abstract State data(ByteBuffer data, MySubNegotiationListener snl);

            public void finish(MySubNegotiationListener snl) {

            }
        }
    }


}
