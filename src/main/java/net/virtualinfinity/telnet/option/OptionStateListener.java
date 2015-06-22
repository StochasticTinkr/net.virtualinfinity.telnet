package net.virtualinfinity.telnet.option;

import net.virtualinfinity.telnet.HasOptionCode;
import net.virtualinfinity.telnet.OptionHandle;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface OptionStateListener {
    /**
     * Called when both DO was requested and WILL was received.
     */
    void enabledRemotely();

    /**
     * Called when either DON'T was requested, or WON'T was received.
     */
    void disabledRemotely();

    /**
     * Called when both WILL was requested and DO was received.
     */
    void enabledLocally();

    /**
     * Called when either WON'T was requested, or DON'T was received.
     */
    void disabledLocally();
}
