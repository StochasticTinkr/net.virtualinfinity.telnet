package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.SubNegotiationListener;
import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface OptionManager {
    void receivedDo(int optionId);
    void receivedDont(int optionId);
    void receivedWill(int optionId);
    void receivedWont(int optionId);
    @Deprecated
    default SubNegotiationListener getSessionHandler(int optionId) {
        return getSubNegotiationListener(optionId);
    }
    SubNegotiationListener getSubNegotiationListener(int optionId);
}
