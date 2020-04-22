package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.SubNegotiationListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.ByteBuffer;

import static org.mockito.Mockito.*;

/**
 * @author Daniel Pitts
 */
@ExtendWith(MockitoExtension.class)
public class CommandRouterTest {
    public static final int OPTION_ID = 10;
    public static final byte[] DATA = {0, 1, 2};

    @Mock
    private SessionListener sessionListener;
    @Mock
    private SubNegotiationDataRouter commandManager;
    @Mock
    private OptionCommandManager optionCommandManager;
    @Mock
    private SubNegotiationListener optionSessionHandler;

    @Test
    public void testReceivedBreak() {
        commandRouter().receivedBreak();
        verify(sessionListener).doBreak();
    }


    @Test
    public void testReceivedInterrupt() {
        commandRouter().receivedInterrupt();
        verify(sessionListener).doInterrupt();
    }

    @Test
    public void testReceivedAbortOutput() {
        commandRouter().receivedAbortOutput();
        verify(sessionListener).doAbortOutput();
    }

    @Test
    public void testReceivedAreYouThere() {
        commandRouter().receivedAreYouThere();
        verify(sessionListener).doAreYouThere();
    }

    @Test
    public void testReceivedEraseCharacter() {
        commandRouter().receivedEraseCharacter();
        verify(sessionListener).doEraseCharacter();
    }

    @Test
    public void testReceivedEraseLine() {
        commandRouter().receivedEraseLine();
        verify(sessionListener).doEraseLine();
    }

    @Test
    public void testReceivedGoAhead() {
        commandRouter().receivedGoAhead();
        verify(sessionListener).doGoAhead();
    }

    @Test
    public void testReceivedEndSubNegotiation() {
        commandRouter().receivedEndSubNegotiation();
        verify(commandManager).receivedEndSubNegotiation();
    }

    @Test
    public void testReceivedData() {
        final ByteBuffer data = ByteBuffer.wrap(DATA);
        commandRouter().receivedData(data);
        verify(commandManager).receivedData(data);
    }

    @Test
    public void testReceivedIAC() {
        commandRouter().receivedIAC();
        verify(commandManager).receivedIAC();
    }

    @Test
    public void testReceivedStartSubNegotiation() {
        when(optionCommandManager.getSubNegotiationListener(OPTION_ID)).thenReturn(optionSessionHandler);
        commandRouter().receivedStartSubNegotiation(OPTION_ID);
        verify(commandManager).receivedStartSubNegotiation(optionSessionHandler);
    }

    @Test
    public void testReceivedDo() {
        commandRouter().receivedDo(OPTION_ID);
        verify(optionCommandManager).receivedDo(OPTION_ID);
    }

    @Test
    public void testReceivedDont() {
        commandRouter().receivedDont(OPTION_ID);
        verify(optionCommandManager).receivedDont(OPTION_ID);
    }

    @Test
    public void testReceivedWill() {
        commandRouter().receivedWill(OPTION_ID);
        verify(optionCommandManager).receivedWill(OPTION_ID);
    }

    @Test
    public void testReceivedWont() {
        commandRouter().receivedWont(OPTION_ID);
        verify(optionCommandManager).receivedWont(OPTION_ID);
    }

    private CommandRouter commandRouter() {
        return new CommandRouter(sessionListener, commandManager, optionCommandManager);
    }
}