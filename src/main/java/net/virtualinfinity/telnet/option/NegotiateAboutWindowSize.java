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

    public static NegotiateAboutWindowSize on(Session session) {
        return create(NegotiateAboutWindowSize::new, session);
    }

    public NegotiateAboutWindowSize addRemoteWindowSizeListener(WindowSizeListener windowSizeListener) {
        windowSizeListeners.add(windowSizeListener);
        return this;
    }

    public NegotiateAboutWindowSize removeRemoteWindowSizeListener(WindowSizeListener windowSizeListener) {
        windowSizeListeners.remove(windowSizeListener);
        return this;
    }

    public NegotiateAboutWindowSize enableRemote() {
        optionHandle.requestRemoteEnable();
        return this;
    }

    public NegotiateAboutWindowSize disableRemote() {
        optionHandle.requestRemoteDisable();
        return this;
    }

    public NegotiateAboutWindowSize startOffering(int initialWidth, int initialHeight ) {
        setWindowSize(initialWidth, initialHeight);
        optionHandle.requestLocalEnable();
        return this;
    }

    public NegotiateAboutWindowSize stopOffering() {
        optionHandle.requestLocalDisable();
        return this;
    }

    @Override
    public void windowSizeReported(int width, int height) {
        setWindowSize(width, height);
    }

    public void setWindowSize(int width, int height) {
        outgoingData.clear();
        outgoingData.order(ByteOrder.BIG_ENDIAN).putShort((short) width).putShort((short) height);
        outgoingData.flip();
        sendUpdate();
    }

    private void sendUpdate() {
        if (optionHandle.isEnabledLocally() && outgoingData.hasRemaining()) {
            final ByteBuffer data = outgoingData.asReadOnlyBuffer();
            sendSubNegotiation(data);
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
                final int width = incomingData.getShort();
                final int height = incomingData.getShort();
                for (final WindowSizeListener listener : windowSizeListeners) {
                    listener.windowSizeReported(width, height);
                }
            }
        }
    }
}
