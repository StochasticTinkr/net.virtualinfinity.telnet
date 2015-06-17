package net.virtualinfinity.telnet;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class OutputChannel {
    private final Consumer<ByteBuffer> output;

    public OutputChannel(Consumer<ByteBuffer> outputBuffer) {
        this.output = outputBuffer;
    }

    public void write(ByteBuffer inputData) {
        ByteBuffer sliced = inputData.slice();
        while (sliced.hasRemaining()) {
            if (sliced.get() == TelnetConstants.IAC) {
                sliced.flip();
                rawWrite(sliced);
                sliced.position(sliced.position() - 1);
                sliced = sliced.slice();
                sliced.position(1);
            }
        }
        sliced.flip();
        rawWrite(sliced);
    }

    public void write(byte[] data) {
        write(ByteBuffer.wrap(data));
    }

    private void rawWrite(ByteBuffer data) {
        output.accept(data);
    }


}
