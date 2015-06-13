package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.handlers.SubNegotiationReceiver;

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
    public ByteBuffer subNegotiationData(ByteBuffer data, TelnetSession session, ByteBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        while (data.hasRemaining() && buffer.hasRemaining()) {
            buffer.put(data.get());
        }

        return buffer;
    }

    @Override
    public ByteBuffer endSubNegotiation(TelnetSession session, ByteBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        buffer.flip();
        if (buffer.limit() > 0 && buffer.get(buffer.limit()-1) == 0) {
            buffer.limit(buffer.limit()-1);
        }
        remoteTerminalTypeListener.terminalSet(Charset.forName("US-ASCII").decode(buffer).toString(), session);

        return null;
    }

    @Override
    public ByteBuffer startSubNegotiation(TelnetSession session, ByteBuffer buffer) {
        return ByteBuffer.allocate(64);
    }

    public void requestNextTerminalType(TelnetSession session) {
        session.sendSubNegotiation(optionCode(), ByteBuffer.wrap(new byte[] { 1 }));
    }
}
