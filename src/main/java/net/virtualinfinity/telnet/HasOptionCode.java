package net.virtualinfinity.telnet;

/**
 * Interface for things that have an option code.
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface HasOptionCode {
    /**
     * Return an option code. These correspond to the option codes for the TELNET protocol.
     *
     * @return a number between 0-255.  Extended option ids are not supported, though none are defined either.
     */
    int optionCode();
}
