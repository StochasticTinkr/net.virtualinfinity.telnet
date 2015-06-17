package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.handlers.OptionReceiver;
import net.virtualinfinity.telnet.option.handlers.SubNegotiationReceiver;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface Options {
    OptionHandle option(HasOptionCode hasOptionCode);
    OptionHandle option(int optionCode);
    <T, R extends SubNegotiationReceiver<T> &OptionReceiver<T>> OptionHandle installOptionReceiver(R receiver);
}
