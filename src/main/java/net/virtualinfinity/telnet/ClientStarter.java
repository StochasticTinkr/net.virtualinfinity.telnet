package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * A helper class to start Telnet sessions which connect to a remote port.
 *
 * @see ConnectionInitiator
 * @author Daniel Pitts
 */
public class ClientStarter {
    private final ConnectionInitiator connectionInitiator;
    public final SessionStarter sessionStarter;

    /**
     * Create a new client starter with a new ConnectionInitiator.
     */
    public ClientStarter() {
        this(new ConnectionInitiator(), SessionStarters.client());
    }

    /**
     * Create a new client starter with the given ConnectionInitiator.
     *
     * @param connectionInitiator the ConnectionInitiator to use.
     * @param sessionStarter the SessionStarter to use to start the session.
     */
    public ClientStarter(ConnectionInitiator connectionInitiator, SessionStarter sessionStarter) {
        this.connectionInitiator = connectionInitiator;
        this.sessionStarter = sessionStarter;
    }

    /**
     * Connect to the given hostname on the default port (23), using the given event loop for managing async operations.
     *
     * @param loop the EventLoop to use
     * @param hostname the hostname to connect to.
     * @param sessionListener The session listener to be informed of session events.
     *
     * @see #connect(EventLoop, String, int, SessionListener)
     */
    public void connect(EventLoop loop, String hostname, SessionListener sessionListener) {
        connect(loop, hostname, 23, sessionListener);
    }

    /**
     * Connect to the given hostname on the given port, using the given event loop for managing async operations.
     * @param loop the EventLoop to use
     * @param hostname the hostname to connect to.
     * @param port the port number.
     * @param sessionListener The session listener to be informed of session events.
     */
    public void connect(EventLoop loop, String hostname, int port, SessionListener sessionListener) {
        final ConnectionListener connectionListener = new ClientConnectionListener(sessionListener);
        connectionInitiator.connect(loop, hostname, port, connectionListener, socketChannel -> startSession(loop, sessionListener, socketChannel));

    }

    private void startSession(EventLoop loop, SessionListener sessionListener, SocketChannel socketChannel) {
        try {
            sessionStarter.startSession(socketChannel, sessionListener, loop);
        } catch (final IOException e) {
            sessionListener.connectionFailed(e);
        }
    }


    /**
     * The connection listener for the connection phase before we've got a session.
     */
    static class ClientConnectionListener implements ConnectionListener {
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

}
