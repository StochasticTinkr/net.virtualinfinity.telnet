package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class CommandDataRouter {
    private final Consumer<ByteBuffer> streamReceiver;
    private OptionSessionHandler<?> optionSessionHandler;
    private Consumer<ByteBuffer> receiver;
    public CommandDataRouter(SessionListener listener) {
        streamReceiver = listener::incomingData;
        receiver = streamReceiver;
    }

    public void receivedEndSubNegotiation() {
        if (optionSessionHandler != null) {
            optionSessionHandler.endSubNegotiation();
            optionSessionHandler = null;
        }
        receiver = streamReceiver;
    }

    public void receivedData(ByteBuffer bytes) {
        receiver.accept(bytes);
    }

    public void receivedIAC() {
        receivedData(ByteBuffer.wrap(new byte[]{TelnetConstants.IAC}));
    }

    public void receivedStartSubNegotiation(OptionSessionHandler<?> optionSessionHandler) {
        this.optionSessionHandler = optionSessionHandler;
        if (optionSessionHandler != null) {
            receiver = optionSessionHandler::subNegotiationData;
            optionSessionHandler.startSubNegotiation();
        } else {
            receiver = byteBuffer -> {};
        }
    }

}
