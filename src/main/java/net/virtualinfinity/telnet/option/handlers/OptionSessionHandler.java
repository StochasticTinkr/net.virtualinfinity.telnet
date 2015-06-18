package net.virtualinfinity.telnet.option.handlers;

import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface OptionSessionHandler<T> {
    void enabledLocally();

    void disabledLocally();

    void enabledRemotely();

    void disabledRemotely();

    void endSubNegotiation();

    void startSubNegotiation();

    void subNegotiationData(ByteBuffer data);
}
