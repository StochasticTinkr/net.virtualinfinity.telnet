package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.SubNegotiationListener;
import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;

import java.nio.ByteBuffer;

/**
 * Routes sub-negotiation data to the right place.
 *
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
interface SubNegotiationDataRouter {
    void receivedEndSubNegotiation();
    void receivedData(ByteBuffer bytes);
    void receivedIAC();
    void receivedStartSubNegotiation(SubNegotiationListener listener);
}
