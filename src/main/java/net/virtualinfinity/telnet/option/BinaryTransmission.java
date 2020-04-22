package net.virtualinfinity.telnet.option;

import net.virtualinfinity.telnet.Option;
import net.virtualinfinity.telnet.Session;

/**
 * Handles the <a href="http://tools.ietf.org/html/rfc856">Binary Transmission</a> Option.
 * It's important to note the interaction of this option with other options which define the "mode".
 *
 * @author Daniel Pitts
 */
public class BinaryTransmission extends AbstractNegotiatingOption {
    private final BinaryOptionListener listener;

    private BinaryTransmission(Session session, BinaryOptionListener listener) {
        super(session, Option.BINARY_TRANSMISSION);
        this.listener = listener;
    }

    /**
     * Attach a new BinaryTransmission option to the given session.
     *
     * @param session The Session.
     * @param listener The listener to be informed of binary transmission requests.
     *
     * @return A new BinaryTransmission object.
     */
    public static BinaryTransmission on(Session session, BinaryOptionListener listener) {
        return create(ses -> new BinaryTransmission(ses, listener), session);
    }

    /**
     * Ask the remote side to send in binary.
     *
     * @return this.
     */
    public BinaryTransmission requestRemoteEnableBinary() {
        optionHandle.requestRemoteEnable();
        return this;
    }

    /**
     * tell the remote side to send in non-binary.
     *
     * @return this.
     */
    public BinaryTransmission requestRemoteDisableBinary() {
        optionHandle.requestRemoteDisable();
        return this;
    }

    /**
     * Suggest to the other side that we will send in binary.
     *
     * @return this.
     */
    public BinaryTransmission suggestLocalSendBinary() {
        optionHandle.requestLocalEnable();
        return this;
    }

    /**
     * Tell the other side we will not send in binary.
     *
     * @return this.
     */
    public BinaryTransmission disableSendingBinary() {
        optionHandle.requestLocalDisable();
        return this;
    }

    /**
     * Allow this side to accept a request to send binary to the remote side.
     *
     * @return this.
     */
    public BinaryTransmission allowLocal() {
        optionHandle.allowLocal();
        return this;
    }

    /**
     * Allow this side to accept an offer to have the remote side send binary.
     *
     * @return this.
     */
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
                listener.remoteWillSendNvtAscii();
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
         *
         * Per the RFC, when "BINARY" mode is disabled, you must enter "NVT ASCII" mode.
         *
         * This is only called if {@link #isRemoteSendingBinary()} returned true and the
         * option was disabled.
         */
        void remoteWillSendNvtAscii();

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
         * Per the RFC, when "BINARY" mode is disabled, you must enter "NVT ASCII" mode.
         *
         * This is only called if {@link #isLocalSendingBinary()} returned true and the
         * option was disabled.
         */
        void localWillSendNvtAscii();

        /**
         * @return true if the current understanding is that the local side is sending binary.
         */
        boolean isLocalSendingBinary();
    }
}
