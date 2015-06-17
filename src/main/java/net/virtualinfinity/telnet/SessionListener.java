package net.virtualinfinity.telnet;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface SessionListener {
    void incomingData(ByteBuffer data);
    void connected(Session session);
    default void connecting() {
    }

    default void doBreak() {
    }

    default void doInterrupt() {
    }

    default void doAbortOutput() {
    }

    default void doAreYouThere() {
    }

    default void doEraseCharacter() {
    }

    default void doEraseLine() {
    }

    default void doGoAhead() {
    }

    default void connectionClosed() {
    }

    default void connectionFailed(IOException e) {
    }

}
