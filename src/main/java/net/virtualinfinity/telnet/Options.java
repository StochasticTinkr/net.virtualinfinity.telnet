package net.virtualinfinity.telnet;

/**
 * Provides access to option handles by option code.
 *
 * @author Daniel Pitts
 */
public interface Options {
    /**
     * Get the OptionHandle for the option code of the given object.
     *
     * @param hasOptionCode an object that has an option code.
     *
     * @return The corresponding OptionHandle. Never null.
     */
    OptionHandle option(HasOptionCode hasOptionCode);

    /**
     * Get the OptionHandle for the given optionCode.
     *
     * @param optionCode the option id
     *
     * @return the option handle.
     */
    OptionHandle option(int optionCode);
}
