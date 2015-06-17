package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.*;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class TelnetClientStarter {
    private final ConnectionInitiator connectionInitiator;

    public TelnetClientStarter() {
        this(new ConnectionInitiator());
    }

    public TelnetClientStarter(ConnectionInitiator connectionInitiator) {
        this.connectionInitiator = connectionInitiator;
    }

    public void connect(EventLoop loop, String hostname, TelnetSessionListener sessionListener) {
        connect(loop, hostname, 23, sessionListener);
    }

    public void connect(EventLoop loop, String hostname, int port, TelnetSessionListener sessionListener) {
        final OutputBuffer outputBuffer = new OutputBuffer();
        final TelnetSession session = new TelnetSession(outputBuffer, sessionListener);
        final ConnectionListener connectionListener = new TelnetSessionConnectionListener(sessionListener, session);
        connectionInitiator.connect(loop, hostname, port, connectionListener, channel -> {
        }, socketChannel -> {
            try {
                createActions(connectionListener, socketChannel, outputBuffer, session).register(loop);
            } catch (ClosedChannelException e) {
                connectionListener.connectionFailed(e);
            }
        });

    }

    public SocketSelectionActions createActions(ConnectionListener connectionListener, SocketChannel socketChannel, OutputBuffer outputBuffer, TelnetSession session) {
        return new SocketSelectionActions(socketChannel, connectionListener, session.createTelnetDataReceiver(), outputBuffer, 2048, false);
    }

    private static class TelnetSessionConnectionListener implements ConnectionListener {
        private final TelnetSessionListener sessionListener;
        private final TelnetSession session;

        public TelnetSessionConnectionListener(TelnetSessionListener sessionListener, TelnetSession session) {
            this.sessionListener = sessionListener;
            this.session = session;
        }

        @Override
        public void connecting() {
        }

        @Override
        public void connected() {
            sessionListener.connected(session);
        }

        @Override
        public void connectionFailed(IOException e) {
            sessionListener.connectionFailed(session, e);
        }

        @Override
        public void disconnected() {
            sessionListener.connectionClosed(session);

        }
    }
}
