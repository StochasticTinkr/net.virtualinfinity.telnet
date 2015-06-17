package net.virtualinfinity.telnet;

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
}
