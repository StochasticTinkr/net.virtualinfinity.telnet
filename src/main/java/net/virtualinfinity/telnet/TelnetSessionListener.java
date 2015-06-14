package net.virtualinfinity.telnet;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface TelnetSessionListener {
    void processData(ByteBuffer data, TelnetSession session) throws IOException;

    default void doBreak(TelnetSession session) {
    }

    default void doInterrupt(TelnetSession session) {
    }

    default void doAbortOutput(TelnetSession session) {
    }

    default void doAreYouThere(TelnetSession session) {
    }

    default void doEraseCharacter(TelnetSession session) {
    }

    default void doEraseLine(TelnetSession session) {
    }

    default void doGoAhead(TelnetSession session) {
    }

    default void connectionClosed(TelnetSession session) {
    }

    default void connectionFailed(TelnetSession session, IOException e) {
    }

    void connected(TelnetSession session);
}
