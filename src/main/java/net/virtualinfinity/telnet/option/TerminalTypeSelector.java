package net.virtualinfinity.telnet.option;

import java.util.Iterator;

/**
 * A local terminal type selector.
 *
 * @see TerminalType
 * @author Daniel Pitts
 */
public interface TerminalTypeSelector extends Iterable<String> {
    /**
     * Returns an iterator over the terminal types.
     *
     * When the {@link Iterator#next()} method is called, the system MUST enter the new terminal emulation mode.
     *
     * @return an iterator over available terminal types.
     */
    Iterator<String> iterator();
}
