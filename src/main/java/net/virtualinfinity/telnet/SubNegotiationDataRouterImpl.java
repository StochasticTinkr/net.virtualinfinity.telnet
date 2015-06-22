package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.SubNegotiationListener;
import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
class SubNegotiationDataRouterImpl implements SubNegotiationDataRouter {
    private final Consumer<ByteBuffer> streamReceiver;
    private SubNegotiationListener optionSessionHandler;
    private Consumer<ByteBuffer> receiver;

    public SubNegotiationDataRouterImpl(SessionListener listener) {
        streamReceiver = listener::incomingData;
        receiver = streamReceiver;
    }

    @Override
    public void receivedEndSubNegotiation() {
        if (optionSessionHandler != null) {
            optionSessionHandler.endSubNegotiation();
            optionSessionHandler = null;
        }
        receiver = streamReceiver;
    }

    @Override
    public void receivedData(ByteBuffer bytes) {
        receiver.accept(bytes);
    }

    @Override
    public void receivedIAC() {
        receivedData(ByteBuffer.wrap(new byte[]{TelnetConstants.IAC}));
    }

    @Override
    public void receivedStartSubNegotiation(SubNegotiationListener optionSessionHandler) {
        this.optionSessionHandler = optionSessionHandler;
        if (optionSessionHandler != null) {
            receiver = optionSessionHandler::subNegotiationData;
            optionSessionHandler.startSubNegotiation();
        } else {
            receiver = byteBuffer -> {};
        }
    }

}
