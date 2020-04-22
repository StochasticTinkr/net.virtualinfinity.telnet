package net.virtualinfinity.telnet.option;

/**
 * A listener for window size reports.
 *
 * @see NegotiateAboutWindowSize#addRemoteWindowSizeListener(WindowSizeListener)
 *
 * @author Daniel Pitts
 */
public interface WindowSizeListener {
    /**
     * Called when the window size has been reported.
     *
     * @param width the new window width
     * @param height the new window height
     */
    void windowSizeReported(int width, int height);
}
