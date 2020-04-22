package net.virtualinfinity.telnet;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Defines an object interested in listening to Telnet Session events.
 *
 * @author Daniel Pitts
 */
public interface SessionListener {
    /**
     * Called when data has been received from the remote end.
     * @param data the data, already decoded and available for the application.
     */
    void incomingData(ByteBuffer data);

    /**
     * Called when the session is connected and available.
     *
     * @param session the session that is connected.
     */
    void connected(Session session);

    /**
     * Called when a connection attempt has been started.
     */
    default void connecting() {
    }

    /**
     * Called when the remote has sent a BRK command.
     */
    default void doBreak() {
    }

    /**
     * Called when the remote has sent an IP command.
     */
    default void doInterrupt() {
    }


    /**
     * Called when the remote has sent an AO command.
     */
    default void doAbortOutput() {
    }

    /**
     * Called when the remote has sent an AYT command.
     */
    default void doAreYouThere() {
    }

    /**
     * Called when the remote has sent an EC command.
     */
    default void doEraseCharacter() {
    }

    /**
     * Called when the remote has sent an EL command.
     */
    default void doEraseLine() {
    }

    /**
     * Called when the remote has sent a GA command.
     */
    default void doGoAhead() {
    }

    /**
     * Called when the connection has been deemed closed.
     */
    default void connectionClosed() {
    }

    /**
     * Called when the connection has failed due to an exception.
     *
     * Note, this may be called either before or after the Session has been set.
     *
     * @param e the exception.
     */
    default void connectionFailed(IOException e) {
    }

}
