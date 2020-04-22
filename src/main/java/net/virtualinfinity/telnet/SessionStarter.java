package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.EventLoop;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * Provides a method for starting a telnet session.
 *
 * @see DefaultSessionStarter
 * @author Daniel Pitts
 */
public interface SessionStarter {
    /**
     * Creates a Telnet Session on the given SocketChannel serviced by the given event loop.
     * @param socketChannel The channel (may have connection pending).
     * @param sessionListener The session listener to be informed about session events.
     * @param loop The event loop used to handle connection.
     *
     * @return A Session.
     *
     * @throws ClosedChannelException if the channel is closed.
     */
    Session startSession(SocketChannel socketChannel, SessionListener sessionListener, EventLoop loop) throws ClosedChannelException;
}
