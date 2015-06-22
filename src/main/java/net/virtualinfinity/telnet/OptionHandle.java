package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.OptionStateListener;
import net.virtualinfinity.telnet.option.SubNegotiationListener;
import net.virtualinfinity.telnet.option.handlers.OptionReceiver;
import net.virtualinfinity.telnet.option.handlers.SubNegotiationReceiver;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface OptionHandle {
    boolean isEnabledRemotely();
    boolean isEnabledLocally();
    OptionHandle requestRemoteEnable();
    OptionHandle requestLocalEnable();
    OptionHandle requestRemoteDisable();
    OptionHandle requestLocalDisable();
    OptionHandle allowLocal();
    OptionHandle allowRemote();
    OptionHandle addStateListener(OptionStateListener optionStateListener);
    OptionHandle removeStateListener(OptionStateListener optionStateListener);
    OptionHandle setSubNegotiationListener(SubNegotiationListener subNegotiationListener);
    int optionCode();
}
