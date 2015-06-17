package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;

import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public abstract class TelnetCommandState {
    private static final TelnetCommandState NORMAL_STATE = new TelnetCommandState() {
        @Override
        public TelnetCommandState data(ByteBuffer bytes, TelnetSession session) {
            if (bytes.hasRemaining()) {
                session.processedData(bytes);
            }
            return NORMAL_STATE;
        }

        @Override
        public TelnetCommandState endSubNegotiation(TelnetSession session) {
            return NORMAL_STATE;
        }
    };

    public abstract TelnetCommandState data(ByteBuffer bytes, TelnetSession session);

    public abstract TelnetCommandState endSubNegotiation(TelnetSession session);

    public static TelnetCommandState initial() {
        return NORMAL_STATE;
    }

    public static TelnetCommandState subNegotiating(OptionSessionHandler<?> optionHandler) {
        return new SubNegotiator(optionHandler);
    }

    private static class SubNegotiator extends TelnetCommandState {
        private final OptionSessionHandler<?> optionHandler;

        public SubNegotiator(OptionSessionHandler<?> optionHandler) {
            this.optionHandler = optionHandler;
        }

        @Override
        public TelnetCommandState data(ByteBuffer bytes, TelnetSession session) {
            if (optionHandler != null) {
                optionHandler.subNegotiationData(bytes, session);
            }
            return this;
        }


        @Override
        public TelnetCommandState endSubNegotiation(TelnetSession session) {
            if (optionHandler != null) {
                optionHandler.endSubNegotiation(session);
            }
            return NORMAL_STATE;
        }
    }
}
