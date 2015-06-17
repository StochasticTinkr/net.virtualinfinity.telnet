package net.virtualinfinity.telnet.option.handlers;

import net.virtualinfinity.telnet.Option;
import net.virtualinfinity.telnet.SubNegotiationOutputChannel;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class RemoteTerminalTypeOption implements SubNegotiationReceiver<ByteBuffer> {
    private final RemoteTerminalTypeListener remoteTerminalTypeListener;

    public RemoteTerminalTypeOption(RemoteTerminalTypeListener remoteTerminalTypeListener) {
        this.remoteTerminalTypeListener = remoteTerminalTypeListener;
    }

    @Override
    public int optionCode() {
        return Option.TERMINAL_TYPE.optionCode();
    }


    @Override
    public ByteBuffer subNegotiationData(ByteBuffer data, ByteBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        while (data.hasRemaining() && buffer.hasRemaining()) {
            buffer.put(data.get());
        }

        return buffer;
    }

    @Override
    public ByteBuffer endSubNegotiation(ByteBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        buffer.flip();
        if (buffer.limit() > 0 && buffer.get(buffer.limit()-1) == 0) {
            buffer.limit(buffer.limit()-1);
        }
        remoteTerminalTypeListener.terminalSet(Charset.forName("US-ASCII").decode(buffer).toString());

        return null;
    }

    @Override
    public ByteBuffer startSubNegotiation(ByteBuffer buffer) {
        return ByteBuffer.allocate(64);
    }

    public void requestNextTerminalType(SubNegotiationOutputChannel output) {
        output.sendSubNegotiation(optionCode(), ByteBuffer.wrap(new byte[] { 1 }));
    }
}
