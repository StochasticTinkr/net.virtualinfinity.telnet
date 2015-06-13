package net.virtualinfinity.telnet.option.handlers;

import net.virtualinfinity.telnet.TelnetSession;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface WindowSizeListener {
    /**
     * Called when the window size has been reported.
     *
     * @param width the new window width
     * @param height the new window height
     * @param session the session.
     */
    void windowSizeReported(int width, int height, TelnetSession session);
}
