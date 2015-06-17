package net.virtualinfinity.telnet.option.handlers;

import net.virtualinfinity.telnet.HasOptionCode;

import java.nio.ByteBuffer;

/**
 * This interface is for handlers of options that were enabled by the local side sending "DO"
 * and the remote side sending "WILL".
 *
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface SubNegotiationReceiver<T> extends HasOptionCode {

    /**
     * Called when an StartSubNegotiation signal was received.
     *
     * @param handlerData option handler specific, unique to the session.
     *
     * @return A new value for the handlerData
     */
    T startSubNegotiation(T handlerData);

    /**
     * Called when data has been sent during a sub-negotiation.
     * @param data The available data.  The buffer is valid only during this call, and should be copied elsewhere if
     * @param handlerData option handler specific, unique to the session.
     *
     * @return A new value for the handlerData
     */
    T subNegotiationData(ByteBuffer data, T handlerData);

    /**
     * Called the remote end reports the sub negotiation data has been sent completely.
     *
     * @param handlerData option handler specific, unique to the session.
     *
     * @return A new value for the handlerData
     */
    T endSubNegotiation(T handlerData);
}
