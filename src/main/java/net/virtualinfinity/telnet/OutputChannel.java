package net.virtualinfinity.telnet;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * Manages sending data to the remote end.  Data is escaped/encoded according to the Telnet spec, except that it is always
 * treated as binary data (no character encoding/decoding happens).
 *
 * @author Daniel Pitts
 */
public class OutputChannel {
    private final Consumer<ByteBuffer> output;

    /**
     * Create an output channel that writes to ByteBuffer consumer.
     *
     * @param output the raw output consumer.
     */
    public OutputChannel(Consumer<ByteBuffer> output) {
        this.output = output;
    }

    /**
     * Writes the data from the inputData buffer to the raw output, escaping anything it needs to.
     *
     * @param inputData the data to send.
     */
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

    /**
     * Writes the data to the raw output, escaping anything it needs to.
     *
     * @param data the data to send.
     */
    public void write(byte[] data) {
        write(ByteBuffer.wrap(data));
    }

    private void rawWrite(ByteBuffer data) {
        output.accept(data);
    }

}
