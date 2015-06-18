package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.*;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class ClientStarter {
    private final ConnectionInitiator connectionInitiator;

    public ClientStarter() {
        this(new ConnectionInitiator());
    }

    public ClientStarter(ConnectionInitiator connectionInitiator) {
        this.connectionInitiator = connectionInitiator;
    }

    public void connect(EventLoop loop, String hostname, SessionListener sessionListener) {
        connect(loop, hostname, 23, sessionListener);
    }

    public void connect(EventLoop loop, String hostname, int port, SessionListener sessionListener) {
        final ConnectionListener connectionListener = new ClientConnectionListener(sessionListener);
        connectionInitiator.connect(loop, hostname, port, connectionListener, socketChannel -> {
                try {
                    final OutputBuffer outputBuffer = new OutputBuffer();
                    final OptionManagerImpl optionManager = new OptionManagerImpl(outputBuffer::append);
                    final OutputChannel outputChannel = new OutputChannel(outputBuffer::append);
                    final SubNegotiationOutputChannel subNegotiationOutputChannel = optionManager.subNegotitationOutputChannel(outputChannel);
                    final Session session = new SessionImpl(optionManager.options(), outputChannel, subNegotiationOutputChannel, socketChannel::close);
                    final CommandRouter commandReceiver = new CommandRouter(sessionListener, new SubNegotiationDataRouterImpl(sessionListener), optionManager);
                    final ClientSessionConnectionListener conListener = new ClientSessionConnectionListener(sessionListener, session);
                    final InputChannelDecoder decoder = new InputChannelDecoder(commandReceiver);
                    final SocketSelectionActions socketSelectionActions = new SocketSelectionActions(socketChannel, conListener, decoder, outputBuffer, 2048, false);

                    socketSelectionActions.register(loop);
                } catch (ClosedChannelException e) {
                    connectionListener.connectionFailed(e);
                }
            });

    }


    private static class ClientConnectionListener implements ConnectionListener {
        protected final SessionListener sessionListener;

        public ClientConnectionListener(SessionListener sessionListener) {
            this.sessionListener = sessionListener;
        }

        @Override
        public void connecting() {
            sessionListener.connecting();
        }

        @Override
        public void connected() {
        }

        @Override
        public void connectionFailed(IOException e) {
            sessionListener.connectionFailed(e);

        }

        @Override
        public void disconnected() {
            sessionListener.connectionClosed();

        }
    }

    private static class ClientSessionConnectionListener extends ClientConnectionListener {
        private final Session session;

        public ClientSessionConnectionListener(SessionListener sessionListener, Session session) {
            super(sessionListener);
            this.session = session;
        }

        @Override
        public void connecting() {
        }

        @Override
        public void connected() {
            sessionListener.connected(session);
        }

    }
}
