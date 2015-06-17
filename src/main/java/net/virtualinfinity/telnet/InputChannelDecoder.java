package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.ByteBufferConsumer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
class InputChannelDecoder implements ByteBufferConsumer {
    private StreamState state = StreamState.initial();
    private final CommandReceiver commandReceiver;

    public InputChannelDecoder(CommandReceiver commandReceiver) {
        this.commandReceiver = commandReceiver;
    }

    @Override
    public void accept(ByteBuffer inputBuffer) throws IOException {
        while (inputBuffer.hasRemaining()) {
            state = state.accept(inputBuffer, commandReceiver);
        }
    }

}
