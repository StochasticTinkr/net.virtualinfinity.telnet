package net.virtualinfinity.telnet;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface TelnetSessionListener {
    void processData(ByteBuffer data, TelnetSession session) throws IOException;
    void doBreak(TelnetSession session);
    void doInterrupt(TelnetSession session);
    void doAbortOutput(TelnetSession session);
    void doAreYouThere(TelnetSession session);
    void doEraseCharacter(TelnetSession session);
    void doEraseLine(TelnetSession session);
    void doGoAhead(TelnetSession session);
}
