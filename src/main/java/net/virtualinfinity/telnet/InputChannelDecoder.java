package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.ByteBufferConsumer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Decodes received data as a telnet stream, sending the data to the given CommandReceiver.
 *
 * @see StreamState
 * @see CommandReceiver
 *
 * @author Daniel Pitts
 */
class InputChannelDecoder implements ByteBufferConsumer {
    private StreamState state = StreamState.initial();
    private final CommandReceiver commandReceiver;

    /**
     * Received data stream will be decoded and sent to the given receiver.
     *
     * @param commandReceiver The command receiver.
     */
    public InputChannelDecoder(CommandReceiver commandReceiver) {
        this.commandReceiver = commandReceiver;
    }

    @Override
    public void accept(ByteBuffer inputBuffer)  {
        while (inputBuffer.hasRemaining()) {
            state = state.accept(inputBuffer, commandReceiver);
        }
    }

}
