package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.OptionStateListener;
import net.virtualinfinity.telnet.option.SubNegotiationListener;

/**
 * Provides an interface to a single telnet option.
 *
 * @see Session#options()
 * @see Options
 * @author Daniel Pitts
 */
public interface OptionHandle extends HasOptionCode {
    /**
     * @return true if this option is enabled on the remote end.
     */
    boolean isEnabledRemotely();

    /**
     * @return true if this option is enabled on the local end.
     */
    boolean isEnabledLocally();

    /**
     * Send a request to the remote side to enable this option on their side.
     *
     * @return this OptionHandle
     */
    OptionHandle requestRemoteEnable();

    /**
     * Send an offer to the remote side to enable this option on our side.
     *
     * @return this OptionHandle
     */
    OptionHandle requestLocalEnable();

    /**
     * Tell the remote end that we no longer want this option enabled on their side.
     *
     * @return this OptionHandle
     */
    OptionHandle requestRemoteDisable();

    /**
     * Tell the remote end that we no longer have this option enabled on our side.
     *
     * @return this OptionHandle
     */
    OptionHandle requestLocalDisable();

    /**
     * Allow the remote side to request this option be enabled on their side.
     * It will be enabled when the remote side next sends a request to enable it.
     *
     * @return this OptionHandle
     */
    OptionHandle allowRemote();

    /**
     * Allow the remote side to request this option be enabled on our side.
     * It will be enabled when the remote side next sends a request to enable it.
     *
     * @return this OptionHandle
     */
    OptionHandle allowLocal();

    /**
     * Add a listener that gets notified of changes in option state.
     *
     * @param optionStateListener the listener.
     *
     * @return this OptionHandle
     */
    OptionHandle addStateListener(OptionStateListener optionStateListener);

    /**
     * Remove a listener.
     *
     * @param optionStateListener the listener.
     *
     * @return this OptionHandle
     */
    OptionHandle removeStateListener(OptionStateListener optionStateListener);

    /**
     * Set an option sub-negotiation listener on this option. There can only be one sub-negotiation listener,
     * so this will overwrite any previously registered listener.
     * @param subNegotiationListener the new listener or null to un-register the current listener.
     *
     * @return this.
     */
    OptionHandle setSubNegotiationListener(SubNegotiationListener subNegotiationListener);

    /**
     * @return The option code this handle is for.
     */
    int optionCode();
}
