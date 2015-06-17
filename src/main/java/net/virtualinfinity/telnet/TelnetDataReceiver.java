package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.ByteBufferConsumer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class TelnetDataReceiver implements ByteBufferConsumer {
    private TelnetStreamState state = TelnetStreamState.initial();
    private final TelnetCommandReceiver commandReceiver;

    public TelnetDataReceiver(TelnetCommandReceiver commandReceiver) {
        this.commandReceiver = commandReceiver;
    }

    @Override
    public void accept(ByteBuffer inputBuffer) throws IOException {
        while (inputBuffer.hasRemaining()) {
            state = state.accept(inputBuffer, commandReceiver);
        }
    }

}
