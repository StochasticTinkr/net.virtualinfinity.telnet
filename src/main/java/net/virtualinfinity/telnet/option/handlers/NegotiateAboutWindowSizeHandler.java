package net.virtualinfinity.telnet.option.handlers;

import net.virtualinfinity.telnet.Option;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.IntConsumer;

/**
 * @author Daniel Pitts
 */
@Deprecated
public class NegotiateAboutWindowSizeHandler implements SubNegotiationReceiver<ByteBuffer> {
    private final WindowSizeListener windowSizeListener;

    public NegotiateAboutWindowSizeHandler(WindowSizeListener windowSizeListener) {
        this.windowSizeListener = windowSizeListener;
    }

    public NegotiateAboutWindowSizeHandler(IntConsumer widthListener) {
        this((width, height) -> widthListener.accept(width));
    }

    @Override
    public int optionCode() {
        return Option.NEGOTIATE_ABOUT_WINDOW_SIZE.optionCode();
    }

    @Override
    public ByteBuffer startSubNegotiation(ByteBuffer sessionData) {
        return ByteBuffer.allocate(4);
    }

    @Override
    public ByteBuffer subNegotiationData(ByteBuffer data, ByteBuffer sessionData) {
        if (sessionData != null) {
            while (sessionData.hasRemaining() && data.hasRemaining()) {
                sessionData.put(data.get());
            }
        }
        return sessionData;
    }

    @Override
    public ByteBuffer endSubNegotiation(ByteBuffer sessionData) {
        if (sessionData != null && !sessionData.hasRemaining() && windowSizeListener != null) {
            sessionData.flip();
            sessionData.order(ByteOrder.BIG_ENDIAN);
            final int width = sessionData.getShort();
            final int height = sessionData.getShort();
            windowSizeListener.windowSizeReported(width, height);
        }
        return null;
    }
}
