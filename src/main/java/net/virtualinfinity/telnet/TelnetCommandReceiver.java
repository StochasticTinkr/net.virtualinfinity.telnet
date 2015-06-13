package net.virtualinfinity.telnet;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
interface TelnetCommandReceiver {
    void receivedDo(int optionId);
    void receivedDont(int optionId);
    void receivedWill(int optionId);
    void receivedWont(int optionId);
    void startSubNegotiation(int optionId);
    void endSubNegotiation();
    void receivedIAC() throws IOException;
    void receivedData(ByteBuffer sliced) throws IOException;
    void receivedBreak();
    void receivedInterrupt();
    void receivedAbortOutput();
    void receivedAreYouThere();
    void receivedEraseCharacter();
    void receiveEraseLine();
    void receivedGoAhead();
}
