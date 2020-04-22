package net.virtualinfinity.telnet.option;

/**
 * A listener that is informed when an option state changes.
 *
 * @see net.virtualinfinity.telnet.OptionHandle#addStateListener(OptionStateListener)
 *
 * @author Daniel Pitts
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
