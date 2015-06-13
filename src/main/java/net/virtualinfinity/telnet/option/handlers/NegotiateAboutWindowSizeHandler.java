package net.virtualinfinity.telnet.option.handlers;

import net.virtualinfinity.telnet.Option;
import net.virtualinfinity.telnet.TelnetSession;

import java.nio.ByteBuffer;
import java.util.function.IntConsumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class NegotiateAboutWindowSizeHandler implements SubNegotiationReceiver<ByteBuffer> {
    private final WindowSizeListener windowSizeListener;

    public NegotiateAboutWindowSizeHandler(WindowSizeListener windowSizeListener) {
        this.windowSizeListener = windowSizeListener;
    }

    public NegotiateAboutWindowSizeHandler(IntConsumer widthListener) {
        this((width, height, session) -> widthListener.accept(width));
    }

    @Override
    public int optionCode() {
        return Option.NEGOTIATE_ABOUT_WINDOW_SIZE.optionCode();
    }

    @Override
    public ByteBuffer startSubNegotiation(TelnetSession session, ByteBuffer sessionData) {
        return ByteBuffer.allocate(4);
    }

    @Override
    public ByteBuffer subNegotiationData(ByteBuffer data, TelnetSession session, ByteBuffer sessionData) {
        if (sessionData != null) {
            while (sessionData.hasRemaining() && data.hasRemaining()) {
                sessionData.put(data.get());
            }
        }
        return sessionData;
    }

    @Override
    public ByteBuffer endSubNegotiation(TelnetSession session, ByteBuffer sessionData) {
        if (sessionData != null && !sessionData.hasRemaining() && windowSizeListener != null) {
            sessionData.flip();
            final int width = readTwo(sessionData);
            final int height = readTwo(sessionData);
            windowSizeListener.windowSizeReported(width, height, session);
        }
        return null;
    }

    private int readTwo(ByteBuffer optionBuffer) {
        final int high = optionBuffer.get() & 255;
        final int low = optionBuffer.get() & 255;
        return high << 8 | low;
    }

}
