package net.virtualinfinity.telnet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

/**
 * @author Daniel Pitts
 */
@ExtendWith(MockitoExtension.class)
public class OptionCommandManagerImplTest {
    public static final byte OPTION_ID = 1;
    @Mock
    private Consumer<ByteBuffer> mockOutput;

    private OptionCommandManagerImpl optionManager;

    @BeforeEach
    public void setUp() {
        optionManager = new OptionCommandManagerImpl(mockOutput);
    }

    @Test
    public void requestRemoteEnableAndAgrees() {
        validateRequestNegotiation(TelnetConstants.DO, optionManager::receivedWill, OptionHandle::isEnabledRemotely, true, OptionHandle::requestRemoteEnable);
    }

    @Test
    public void requestRemoteEnableAndDisagrees() {
        validateRequestNegotiation(TelnetConstants.DO, optionManager::receivedWont, OptionHandle::isEnabledRemotely, false, OptionHandle::requestRemoteEnable);
    }

    @Test
    public void requestLocalEnableAndAgrees() {
        validateRequestNegotiation(TelnetConstants.WILL, optionManager::receivedDo, OptionHandle::isEnabledLocally, true, OptionHandle::requestLocalEnable);
    }

    @Test
    public void requestLocalEnableAndDisagrees() {
        validateRequestNegotiation(TelnetConstants.WILL, optionManager::receivedDont, OptionHandle::isEnabledLocally, false, OptionHandle::requestLocalEnable);
    }

    @Test
    public void respondToNotAllowedDo() {
        optionManager.receivedDo(OPTION_ID);
        verifyCommandSent(TelnetConstants.WONT);
    }

    @Test
    public void respondToNotAllowedWill() {
        optionManager.receivedWill(OPTION_ID);
        verifyCommandSent(TelnetConstants.DONT);
    }

    @Test
    public void respondToAllowedWill() {
        option().allowRemote();
        optionManager.receivedWill(OPTION_ID);
        verifyCommandSent(TelnetConstants.DO);
    }

    @Test
    public void respondToAllowedDo() {
        option().allowLocal();
        optionManager.receivedDo(OPTION_ID);
        verifyCommandSent(TelnetConstants.WILL);

    }
    public Options options() {
        return optionManager.options();
    }

    public OptionHandle option() {
        return options().option(OPTION_ID);
    }

    public void validateRequestNegotiation(byte command, IntConsumer reply, Predicate<OptionHandle> enabledChecker, boolean endsEnabled, Consumer<OptionHandle> request) {
        assertFalse(enabledChecker.test(option()));
        request.accept(option());
        assertFalse(enabledChecker.test(option()));
        reply.accept(OPTION_ID);
        assertEquals(enabledChecker.test(option()), endsEnabled);
        verifyCommandSent(command);
    }

    private void verifyCommandSent(byte command) {
        verify(mockOutput).accept(ByteBuffer.wrap(new byte[]{TelnetConstants.IAC, command, OPTION_ID}));
    }
}