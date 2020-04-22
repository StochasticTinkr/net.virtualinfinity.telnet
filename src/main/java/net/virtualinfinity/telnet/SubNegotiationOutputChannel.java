package net.virtualinfinity.telnet;

import java.nio.ByteBuffer;

/**
 * Provides a channel for sending sub-negotiation data.  The data is properly escaped/encoded before being sent.
 *
 * @author Daniel Pitts
 */
public interface SubNegotiationOutputChannel {
    /**
     * Sends a sub-negotiation. This method sends the IAC SB <i>optionId</i>, the encoded/escaped <code>data</code>, and then
     * an IAC SE.  There is no way to send a partial sub-negotiation, so the entire message needs to be constructed at once.
     *
     * @param optionId the option id.
     * @param data the data.
     */
    void sendSubNegotiation(int optionId, ByteBuffer data);
}
