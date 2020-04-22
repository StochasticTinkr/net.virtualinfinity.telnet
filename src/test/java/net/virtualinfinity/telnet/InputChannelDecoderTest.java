package net.virtualinfinity.telnet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

/**
 * @author Daniel Pitts
 */
@ExtendWith(MockitoExtension.class)
public class InputChannelDecoderTest {
    public static final byte OPTION_ID = 10;
    public static final byte EXTRA_DATA1 = 44;
    public static final byte EXTRA_DATA2 = 45;
    public static final byte[] EXTRA_DATA1_ARRAY = {EXTRA_DATA1};
    public static final byte[] EXTRA_DATA2_ARRAY = {EXTRA_DATA2};

    @Mock
    private CommandReceiver commandReceiver;

    @Test
    public void receivedDo() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.DO, OPTION_ID, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedDo);
    }

    @Test
    public void receivedDont() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.DONT, OPTION_ID, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedDont);
    }

    @Test
    public void receivedWill() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.WILL, OPTION_ID, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedWill);
    }

    @Test
    public void receivedWont() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.WONT, OPTION_ID, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedWont);
    }

    @Test
    public void receivedStartSubNegotiation() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.SB, OPTION_ID, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedStartSubNegotiation);
    }

    @Test
    public void receivedEndSubNegotiation() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.SE, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedEndSubNegotiation);
    }

    @Test
    public void receivedIAC() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.IAC, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedIAC);
    }

    @Test
    public void receivedData() {
        acceptData(EXTRA_DATA1, EXTRA_DATA2);
        verify(commandReceiver).receivedData(ByteBuffer.wrap(new byte[]{EXTRA_DATA1, EXTRA_DATA2}));
    }

    @Test
    public void receivedBreak() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.BRK, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedBreak);
    }

    @Test
    public void receivedInterrupt() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.IP, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedInterrupt);
    }

    @Test
    public void receivedAbortOutput() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.AO, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedAbortOutput);
    }

    @Test
    public void receivedAreYouThere() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.AYT, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedAreYouThere);
    }

    @Test
    public void receivedEraseCharacter() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.EC, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedEraseCharacter);
    }

    @Test
    public void receivedEraseLine() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.EL, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedEraseLine);
    }

    @Test
    public void receivedGoAhead() {
        acceptData(EXTRA_DATA1, TelnetConstants.IAC, TelnetConstants.GA, EXTRA_DATA2);
        verifyReceived(CommandReceiver::receivedGoAhead);
    }

    private void acceptData(byte... data) {
        final ByteBuffer wrap = ByteBuffer.wrap(data);
        new InputChannelDecoder(commandReceiver).accept(wrap);
        assertFalse(wrap.hasRemaining());
    }
    private void verifyReceived(ObjIntConsumer<CommandReceiver> command) {
        verifyReceived(commandReceiver -> command.accept(commandReceiver, OPTION_ID));
    }

    private void verifyReceived(Consumer<CommandReceiver> command) {
        InOrder inOrder = inOrder(commandReceiver);
        inOrder.verify(commandReceiver).receivedData(ByteBuffer.wrap(EXTRA_DATA1_ARRAY));
        command.accept(inOrder.verify(commandReceiver));
        inOrder.verify(commandReceiver).receivedData(ByteBuffer.wrap(EXTRA_DATA2_ARRAY));

    }

}