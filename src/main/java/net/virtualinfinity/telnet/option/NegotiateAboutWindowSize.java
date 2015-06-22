package net.virtualinfinity.telnet.option;

import net.virtualinfinity.telnet.Option;
import net.virtualinfinity.telnet.Session;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implements the <a href="http://tools.ietf.org/html/rfc1073" title="RFC-1073">Telnet Window Size Option</a>.
 *
 * This allows you to send window size to the remote end, if they care about it, and it also allows you to request
 * the remote end send you window size updates.
 *
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public final class NegotiateAboutWindowSize extends AbstractNegotiatingOption implements WindowSizeListener {
    private final Collection<WindowSizeListener> windowSizeListeners = new ArrayList<>();

    private final ByteBuffer outgoingData = ByteBuffer.allocate(4);

    private NegotiateAboutWindowSize(Session session) {
        super(session, Option.NEGOTIATE_ABOUT_WINDOW_SIZE);
    }

    /**
     * Attaches a NegotiateAboutWindowSize option to the given session.
     *
     * @param session The session to attach to.
     *
     * @return a new NegotiateAboutWindowSize.
     */
    public static NegotiateAboutWindowSize on(Session session) {
        return create(NegotiateAboutWindowSize::new, session);
    }

    /**
     * Adds a listener to be informed about remote size reports.
     *
     * @param windowSizeListener The listener to be registered.
     *
     * @return this.
     */
    public NegotiateAboutWindowSize addRemoteWindowSizeListener(WindowSizeListener windowSizeListener) {
        windowSizeListeners.add(windowSizeListener);
        return this;
    }

    /**
     * Removes a previously registered listener.
     *
     * @param windowSizeListener The listener to be de-registered.
     *
     * @return this.
     */
    public NegotiateAboutWindowSize removeRemoteWindowSizeListener(WindowSizeListener windowSizeListener) {
        windowSizeListeners.remove(windowSizeListener);
        return this;
    }

    /**
     * Request that the remote end start sending its window size.
     *
     * @return this.
     */
    public NegotiateAboutWindowSize enableRemote() {
        optionHandle.requestRemoteEnable();
        return this;
    }

    /**
     * Tell the remote to stop sending its window size.
     *
     * @return this.
     */
    public NegotiateAboutWindowSize disableRemote() {
        optionHandle.requestRemoteDisable();
        return this;
    }

    /**
     * Offer the start telling our window size.
     *
     * @param initialWidth the initial width to send if our offer is accepted.
     * @param initialHeight the initial height to send if our offer is accepted.
     *
     * @return this.
     */
    public NegotiateAboutWindowSize startOffering(int initialWidth, int initialHeight ) {
        setWindowSize(initialWidth, initialHeight);
        optionHandle.requestLocalEnable();
        return this;
    }

    /**
     * Allow the remote to request us telling our window size.
     *
     * @param initialWidth the initial width to send if our offer is accepted.
     * @param initialHeight the initial height to send if our offer is accepted.
     *
     * @return this.
     */
    public NegotiateAboutWindowSize allowOffering(int initialWidth, int initialHeight ) {
        setWindowSize(initialWidth, initialHeight);
        optionHandle.allowLocal();
        return this;
    }

    /**
     * Stop sending our window size.
     *
     * @return this.
     */
    public NegotiateAboutWindowSize stopOffering() {
        optionHandle.requestLocalDisable();
        return this;
    }

    /**
     * Convenience method that implements WindowSizeListener. This calls {@link #setWindowSize(int, int)}.
     *
     * @param width the new window width
     * @param height the new window height
     */
    @Override
    public void windowSizeReported(int width, int height) {
        setWindowSize(width, height);
    }

    /**
     * Sets the current window size.  If we are currently offering our window size, the remote end will be notified
     * of this new size.
     *
     * @param width the new window width
     * @param height the new window height
     */
    public void setWindowSize(int width, int height) {
        outgoingData.clear();
        outgoingData.order(ByteOrder.BIG_ENDIAN).putShort((short) width).putShort((short) height);
        outgoingData.flip();
        sendUpdate();
    }

    /**
     * Sends the update if we have one and the option is enabled.
     */
    private void sendUpdate() {
        if (optionHandle.isEnabledLocally() && outgoingData.hasRemaining()) {
            sendSubNegotiation(outgoingData.asReadOnlyBuffer());
        }
    }

    @Override
    protected MySubNegotiationListener getNegotiationListener() {
        return new MySubNegotiationListener();
    }

    @Override
    protected MyOptionStateListener getStateListener() {
        return new MyOptionStateListener();
    }

    /**
     * Sends the update when the option becomes enabled.
     */
    private class MyOptionStateListener implements OptionStateListener {
        @Override
        public void enabledRemotely() {
        }

        @Override
        public void disabledRemotely() {
        }

        @Override
        public void enabledLocally() {
            sendUpdate();
        }

        @Override
        public void disabledLocally() {
        }
    }

    /**
     * Manages listening for incoming NAWS reports.
     */
    private class MySubNegotiationListener implements SubNegotiationListener {
        private final ByteBuffer incomingData = ByteBuffer.allocate(4);

        @Override
        public void startSubNegotiation() {
            incomingData.clear();
        }

        @Override
        public void subNegotiationData(ByteBuffer data) {
            incomingData.put(data);
        }

        @Override
        public void endSubNegotiation() {
            incomingData.flip();
            if (incomingData.remaining() == 4) {
                incomingData.order(ByteOrder.BIG_ENDIAN);
                // 0xFFFFF is to force unsigned short.
                final int width = incomingData.getShort() & 0xFFFF;
                final int height = incomingData.getShort() & 0xFFFF;
                for (final WindowSizeListener listener : windowSizeListeners) {
                    listener.windowSizeReported(width, height);
                }
            }
        }
    }
}
