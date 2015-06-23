package net.virtualinfinity.telnet.option;

import java.nio.ByteBuffer;

/**
 * A listener that gets called during option sub-negotiation.
 *
 * @see net.virtualinfinity.telnet.OptionHandle#setSubNegotiationListener(SubNegotiationListener)
 *
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface SubNegotiationListener {
    /**
     * Called when we received a IAC SB <i>option</i> signal from the remote side.
     * This indicates that a sub-negotiation is about to begin.
     */
    void startSubNegotiation();

    /**
     * Called when data has been sent during a sub-negotiation. The data is the stream between the
     * IAC SB <i>option</i> and IAC SE <i>option</i> signals.
     *
     * This function may be called multiple times as more data becomes available, but the buffer will be cleared
     * between calls, so you must complete any processing you need to, or copy the data into another buffer.
     *
     * @param data The available data.  The buffer is valid only during this call, and should be copied elsewhere if needed.
     */
    void subNegotiationData(ByteBuffer data);

    /**
     * Called when IAC SE was received.
     */
    void endSubNegotiation();
}
