package net.virtualinfinity.telnet;

import junit.framework.AssertionFailedError;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Auto;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.internal.ExpectationBuilder;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class InputChannelDecoderTest {
    public static final byte OPTION_ID = 10;
    public static final byte EXTRA_DATA = 44;
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Mock
    private CommandReceiver commandReceiver;

    @Auto
    private Sequence sequence;

    @Test
    public void receivedDo() {
        context.checking(expectOptionCommand(CommandReceiver::receivedDo));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.DO, OPTION_ID, EXTRA_DATA);
    }

    @Test
    public void receivedDont() {
        context.checking(expectOptionCommand(CommandReceiver::receivedDont));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.DONT, OPTION_ID, EXTRA_DATA);
    }

    @Test
    public void receivedWill() {
        context.checking(expectOptionCommand(CommandReceiver::receivedWill));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.WILL, OPTION_ID, EXTRA_DATA);
    }

    @Test
    public void receivedWont() {
        context.checking(expectOptionCommand(CommandReceiver::receivedWont));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.WONT, OPTION_ID, EXTRA_DATA);
    }

    @Test
    public void receivedStartSubNegotiation() {
        context.checking(expectOptionCommand(CommandReceiver::receivedStartSubNegotiation));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.SB, OPTION_ID, EXTRA_DATA);
    }

    @Test
    public void receivedEndSubNegotiation() {
        context.checking(expectSimpleCommand(CommandReceiver::receivedEndSubNegotiation));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.SE, EXTRA_DATA);
    }

    @Test
    public void receivedIAC() {
        context.checking(expectSimpleCommand(CommandReceiver::receivedIAC));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.IAC, EXTRA_DATA);
    }

    @Test
    public void receivedData() {
        context.checking(new Expectations() {{
            oneOf(commandReceiver).receivedData(ByteBuffer.wrap(new byte[]{EXTRA_DATA})); inSequence(sequence);
        }});
        acceptData(EXTRA_DATA);
    }

    @Test
    public void receivedBreak() {
        context.checking(expectSimpleCommand(CommandReceiver::receivedBreak));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.BRK, EXTRA_DATA);
    }
    @Test
    public void receivedInterrupt() {
        context.checking(expectSimpleCommand(CommandReceiver::receivedInterrupt));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.IP, EXTRA_DATA);
    }

    @Test
    public void receivedAbortOutput() {
        context.checking(expectSimpleCommand(CommandReceiver::receivedAbortOutput));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.AO, EXTRA_DATA);
    }

    @Test
    public void receivedAreYouThere() {
        context.checking(expectSimpleCommand(CommandReceiver::receivedAreYouThere));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.AYT, EXTRA_DATA);
    }

    @Test
    public void receivedEraseCharacter() {
        context.checking(expectSimpleCommand(CommandReceiver::receivedEraseCharacter));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.EC, EXTRA_DATA);
    }

    @Test
    public void receivedEraseLine() {
        context.checking(expectSimpleCommand(CommandReceiver::receivedEraseLine));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.EL, EXTRA_DATA);
    }

    @Test
    public void receivedGoAhead() {
        context.checking(expectSimpleCommand(CommandReceiver::receivedGoAhead));
        acceptData(EXTRA_DATA, TelnetConstants.IAC, TelnetConstants.GA, EXTRA_DATA);
    }

    private void acceptData(byte...data) {
        try {
            new InputChannelDecoder(commandReceiver).accept(ByteBuffer.wrap(data));
        } catch (final IOException ioException) {
            final AssertionFailedError error = new AssertionFailedError("Unexpected IOException");
            error.addSuppressed(ioException);
            throw error;
        }
    }

    private ExpectationBuilder expectOptionCommand(final ObjIntConsumer<CommandReceiver> optionCommand) {
        return expectSimpleCommand(commandReceiver -> optionCommand.accept(commandReceiver, OPTION_ID));
    }
    private ExpectationBuilder expectSimpleCommand(final Consumer<CommandReceiver> command) {
        return new Expectations() {{
            oneOf(commandReceiver).receivedData(ByteBuffer.wrap(new byte[]{EXTRA_DATA})); inSequence(sequence);
            command.accept(oneOf(commandReceiver)); inSequence(sequence);
            oneOf(commandReceiver).receivedData(ByteBuffer.wrap(new byte[]{EXTRA_DATA})); inSequence(sequence);
        }};
    }



}