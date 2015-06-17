package net.virtualinfinity.telnet;

import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface SubNegotiationOutputChannel {
    void sendSubNegotiation(int optionId, ByteBuffer data);
}
