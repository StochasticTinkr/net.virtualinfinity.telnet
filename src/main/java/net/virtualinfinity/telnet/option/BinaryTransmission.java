package net.virtualinfinity.telnet.option;

import net.virtualinfinity.telnet.Option;
import net.virtualinfinity.telnet.Session;

/**
 * Handles the <a href="http://tools.ietf.org/html/rfc856">Binary Transmission</a> Option.
 * It's important to note the interaction of this option with other options which define the "mode".
 *
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class BinaryTransmission extends AbstractNegotiatingOption {
    private final BinaryOptionListener listener;

    private BinaryTransmission(Session session, BinaryOptionListener listener) {
        super(session, Option.BINARY_TRANSMISSION);
        this.listener = listener;
    }

    public static BinaryTransmission on(Session session, BinaryOptionListener listener) {
        return create(ses -> new BinaryTransmission(ses, listener), session);
    }

    public BinaryTransmission requestRemoteEnableBinary() {
        optionHandle.requestRemoteEnable();
        return this;
    }

    public BinaryTransmission requestRemoteDisableBinary() {
        optionHandle.requestRemoteDisable();
        return this;
    }

    public BinaryTransmission suggestLocalSendBinary() {
        optionHandle.requestLocalEnable();
        return this;
    }

    public BinaryTransmission disableSendingBinary() {
        optionHandle.requestLocalDisable();
        return this;
    }

    public BinaryTransmission allowLocal() {
        optionHandle.allowLocal();
        return this;
    }
    public BinaryTransmission allowRemote() {
        optionHandle.allowRemote();
        return this;
    }

    @Override
    protected SubNegotiationListener getNegotiationListener() {
        return null;
    }

    @Override
    protected OptionStateListener getStateListener() {
        return listener != null ? new Listener(listener) : null;
    }


    private static class Listener implements OptionStateListener {
        final BinaryOptionListener listener;
        public Listener(BinaryOptionListener listener) {
            this.listener = listener;
        }

        @Override
        public void enabledLocally() {
            listener.localWillSendBinary();
        }

        @Override
        public void disabledLocally() {
            if (listener.isLocalSendingBinary()) {
                listener.localWillSendNvtAscii();
            }
        }

        @Override
        public void enabledRemotely() {
            listener.remoteWillSendBinary();
        }

        @Override
        public void disabledRemotely() {
            if (listener.isRemoteSendingBinary()) {
                listener.remoteWillNvtAscii();
            }
        }
    }

    public interface BinaryOptionListener {
        /**
         * Called when both sides have agreed that the remote side should send binary.
         */
        void remoteWillSendBinary();

        /**
         * Called when the remote side was sending binary, but has demanded to stop.
         */
        void remoteWillNvtAscii();

        /**
         * @return true if the current understanding is that the remote side is sending binary.
         */
        boolean isRemoteSendingBinary();
        /**
         * Called when both sides have agreed that the local side should send binary.
         */
        void localWillSendBinary();
        /**
         * Called when the local side was sending binary, but has demanded to stop.
         */
        void localWillSendNvtAscii();

        /**
         * @return true if the current understanding is that the local side is sending binary.
         */
        boolean isLocalSendingBinary();
    }
}
