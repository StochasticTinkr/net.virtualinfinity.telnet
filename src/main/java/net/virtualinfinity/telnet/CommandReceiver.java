package net.virtualinfinity.telnet;

import java.nio.ByteBuffer;

/**
 * Receives the raw commands as decoded by the {@link InputChannelDecoder}.
 *
 * @author Daniel Pitts
 */
interface CommandReceiver {
    /**
     * Received IAC DO <i>optionId</i>
     *
     * @param optionId The option ID.
     */
    void receivedDo(int optionId);
    /**
     * Received IAC DON'T <i>optionId</i>
     *
     * @param optionId The option ID.
     */
    void receivedDont(int optionId);
    /**
     * Received IAC WILL <i>optionId</i>
     *
     * @param optionId The option ID.
     */
    void receivedWill(int optionId);

    /**
     * Received IAC WON'T <i>optionId</i>
     *
     * @param optionId The option ID.
     */
    void receivedWont(int optionId);

    /**
     * Received IAC SB <i>optionId</i>
     *
     * @param optionId The option ID.
     */
    void receivedStartSubNegotiation(int optionId);
    /**
     * Received IAC SE
     */
    void receivedEndSubNegotiation();

    /**
     * Received IAC IAC
     */
    void receivedIAC();

    /**
     * Received data.
     *
     * @param data This data is guaranteed to not have an IAC in it.
     */
    void receivedData(ByteBuffer data);

    /**
     * Received IAC BRK
     */
    void receivedBreak();

    /**
     * Received IAC IP
     */
    void receivedInterrupt();

    /**
     * Received IAC AO
     */
    void receivedAbortOutput();

    /**
     * Received IAC AYT
     */
    void receivedAreYouThere();

    /**
     * Received IAC EC
     */
    void receivedEraseCharacter();

    /**
     * Received IAC EL
     */
    void receivedEraseLine();

    /**
     * Received IAC GA
     */
    void receivedGoAhead();
}
