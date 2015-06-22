package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.SubNegotiationListener;

/**
 * Manages the state of options.
 *
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
interface OptionCommandManager {
    /**
     * Received a DO command.
     * @param optionId the option.
     */
    void receivedDo(int optionId);

    /**
     * Received a DON'T command.
     *
     * @param optionId the option.
     */
    void receivedDont(int optionId);

    /**
     * Received a WILL command.
     *
     * @param optionId the option.
     */
    void receivedWill(int optionId);

    /**
     * Received a WON'T  command.
     * @param optionId the option.
     */
    void receivedWont(int optionId);

    /**
     * Get the sub-negotiation command listener for a particular option.
     *
     * @param optionId the option.
     *
     * @return the listener, or null if none.
     */
    SubNegotiationListener getSubNegotiationListener(int optionId);
}
