package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.SubNegotiationListener;
import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class CommandRouterTest {
    public static final int OPTION_ID = 10;
    public static final byte[] DATA = {0, 1, 2};
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Mock
    private SessionListener sessionListener;
    @Mock
    private SubNegotiationDataRouter commandManager;
    @Mock
    private OptionManager optionManager;
    @Mock
    private SubNegotiationListener optionSessionHandler;

    @Test
    public void testReceivedBreak() throws Exception {
        context.checking(oneOf(sessionListener, SessionListener::doBreak));
        commandRouter().receivedBreak();
    }


    @Test
    public void testReceivedInterrupt() throws Exception {
        context.checking(oneOf(sessionListener, SessionListener::doInterrupt));
        commandRouter().receivedInterrupt();
    }

    @Test
    public void testReceivedAbortOutput() throws Exception {
        context.checking(oneOf(sessionListener, SessionListener::doAbortOutput));
        commandRouter().receivedAbortOutput();
    }

    @Test
    public void testReceivedAreYouThere() throws Exception {
        context.checking(oneOf(sessionListener, SessionListener::doAreYouThere));
        commandRouter().receivedAreYouThere();
    }

    @Test
    public void testReceivedEraseCharacter() throws Exception {
        context.checking(oneOf(sessionListener, SessionListener::doEraseCharacter));
        commandRouter().receivedEraseCharacter();
    }

    @Test
    public void testReceivedEraseLine() throws Exception {
        context.checking(oneOf(sessionListener, SessionListener::doEraseLine));
        commandRouter().receivedEraseLine();
    }

    @Test
    public void testReceivedGoAhead() throws Exception {
        context.checking(oneOf(sessionListener, SessionListener::doGoAhead));
        commandRouter().receivedGoAhead();
    }

    @Test
    public void testReceivedEndSubNegotiation() throws Exception {
        context.checking(oneOf(commandManager, SubNegotiationDataRouter::receivedEndSubNegotiation));
        commandRouter().receivedEndSubNegotiation();
    }

    @Test
    public void testReceivedData() throws Exception {
        context.checking(oneOf(commandManager, cm -> cm.receivedData(ByteBuffer.wrap(DATA))));
        commandRouter().receivedData(ByteBuffer.wrap(DATA));
    }

    @Test
    public void testReceivedIAC() throws Exception {
        context.checking(oneOf(commandManager, SubNegotiationDataRouter::receivedIAC));
        commandRouter().receivedIAC();
    }

    @Test
    public void testReceivedStartSubNegotiation() throws Exception {
        context.checking(new Expectations() {{
            oneOf(optionManager).getSubNegotiationListener(OPTION_ID); will(returnValue(optionSessionHandler));
            oneOf(commandManager).receivedStartSubNegotiation(optionSessionHandler);
        }});
        commandRouter().receivedStartSubNegotiation(OPTION_ID);
    }

    @Test
    public void testReceivedDo() throws Exception {
        context.checking(oneOf(optionManager, OptionManager::receivedDo, OPTION_ID));
        commandRouter().receivedDo(OPTION_ID);
    }

    @Test
    public void testReceivedDont() throws Exception {
        context.checking(oneOf(optionManager, OptionManager::receivedDont, OPTION_ID));
        commandRouter().receivedDont(OPTION_ID);
    }

    @Test
    public void testReceivedWill() throws Exception {
        context.checking(oneOf(optionManager, OptionManager::receivedWill, OPTION_ID));
        commandRouter().receivedWill(OPTION_ID);
    }

    @Test
    public void testReceivedWont() throws Exception {
        context.checking(oneOf(optionManager, OptionManager::receivedWont, OPTION_ID));
        commandRouter().receivedWont(OPTION_ID);
    }

    private CommandRouter commandRouter() {
        return new CommandRouter(sessionListener, commandManager, optionManager);
    }

    private  <T> Expectations oneOf(final T mock, final Consumer<T> action) {
        return new Expectations() {{
            action.accept(oneOf(mock));
        }};
    }
    private  <T> Expectations oneOf(final T mock, final ObjIntConsumer<T> action, int param) {
        return new Expectations() {{
            action.accept(oneOf(mock), param);
        }};
    }

}