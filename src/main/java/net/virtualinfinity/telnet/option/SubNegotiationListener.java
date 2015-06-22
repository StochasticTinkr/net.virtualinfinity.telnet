package net.virtualinfinity.telnet.option;

import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface SubNegotiationListener {
    /**
     * Called when an StartSubNegotiation signal was received.
     */
    void startSubNegotiation();

    /**
     * Called when data has been sent during a sub-negotiation.
     * @param data The available data.  The buffer is valid only during this call, and should be copied elsewhere if needed.
     */
    void subNegotiationData(ByteBuffer data);

    /**
     * Called the remote end reports the sub negotiation data has been sent completely.
     */
    void endSubNegotiation();
}
