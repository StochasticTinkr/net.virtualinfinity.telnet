package net.virtualinfinity.telnet;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface Options {
    OptionHandle option(HasOptionCode hasOptionCode);
    OptionHandle option(int optionCode);
}
