package net.virtualinfinity.telnet;

import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
interface CommandReceiver {
    void receivedDo(int optionId);
    void receivedDont(int optionId);
    void receivedWill(int optionId);
    void receivedWont(int optionId);
    void receivedStartSubNegotiation(int optionId);
    void receivedEndSubNegotiation();
    void receivedIAC();
    void receivedData(ByteBuffer data);
    void receivedBreak();
    void receivedInterrupt();
    void receivedAbortOutput();
    void receivedAreYouThere();
    void receivedEraseCharacter();
    void receivedEraseLine();
    void receivedGoAhead();
}
