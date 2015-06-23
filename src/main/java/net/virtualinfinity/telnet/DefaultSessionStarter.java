package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.EventLoop;
import net.virtualinfinity.nio.OutputBuffer;
import net.virtualinfinity.nio.SocketChannelInterface;
import net.virtualinfinity.nio.SocketSelectionActions;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

/**
 * Provides a default implementation of the SessionStarter.
 *
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
class DefaultSessionStarter implements SessionStarter {
    private final int inputBufferSize;
    private final boolean sendAllBeforeReading;


    /**
     * Creates a session starter that
     */
    DefaultSessionStarter() {
        this(2048, false);
    }

    public DefaultSessionStarter(int inputBufferSize, boolean sendAllBeforeReading) {
        this.inputBufferSize = inputBufferSize;
        this.sendAllBeforeReading = sendAllBeforeReading;
    }

    @Override
    public Session startSession(SocketChannelInterface socketChannel, SessionListener sessionListener, EventLoop loop) throws ClosedChannelException {
        final OutputBuffer outputBuffer = new OutputBuffer();
        final OptionCommandManagerImpl optionManager = new OptionCommandManagerImpl(outputBuffer::append);
        final OutputChannel outputChannel = new OutputChannel(outputBuffer::append);
        final SubNegotiationOutputChannel subNegotiationOutputChannel = optionManager.subNegotiationOutputChannel(outputChannel);
        final Session session = new SessionImpl(optionManager.options(), outputChannel, subNegotiationOutputChannel, socketChannel::close);
        final CommandRouter commandReceiver = new CommandRouter(sessionListener, new SubNegotiationDataRouterImpl(sessionListener), optionManager);
        final ClientSessionConnectionListener conListener = new ClientSessionConnectionListener(sessionListener, session);
        final InputChannelDecoder decoder = new InputChannelDecoder(commandReceiver);
        final SocketSelectionActions socketSelectionActions = new SocketSelectionActions(socketChannel, conListener, decoder, outputBuffer, inputBufferSize, sendAllBeforeReading);
        socketSelectionActions.register(loop);
        return session;
    }

    /**
     * The connection listener for the phase after we've got a session.
     */
    static class ClientSessionConnectionListener extends ClientStarter.ClientConnectionListener {
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
