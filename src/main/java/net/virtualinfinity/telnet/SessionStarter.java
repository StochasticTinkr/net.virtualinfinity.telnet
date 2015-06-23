package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.EventLoop;
import net.virtualinfinity.nio.SocketChannelInterface;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

/**
 * Provides a method for starting a telnet session.
 *
 * @see DefaultSessionStarter
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
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
    Session startSession(SocketChannelInterface socketChannel, SessionListener sessionListener, EventLoop loop) throws ClosedChannelException;
}
