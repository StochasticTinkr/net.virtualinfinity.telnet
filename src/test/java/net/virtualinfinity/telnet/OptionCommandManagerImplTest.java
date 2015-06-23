package net.virtualinfinity.telnet;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.internal.ExpectationBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class OptionCommandManagerImplTest {
    public static final byte OPTION_ID = 1;
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Mock
    private Consumer<ByteBuffer> output;
    private OptionCommandManagerImpl optionManager;

    @Before
    public void setUp() throws Exception {
        optionManager = new OptionCommandManagerImpl(output);
    }

    @Test
    public void requestRemoteEnableAndAgrees() throws Exception {
        validateRequestNegotiation(TelnetConstants.DO, optionManager::receivedWill, OptionHandle::isEnabledRemotely, true, OptionHandle::requestRemoteEnable);
    }

    @Test
    public void requestRemoteEnableAndDisagrees() throws Exception {
        validateRequestNegotiation(TelnetConstants.DO, optionManager::receivedWont, OptionHandle::isEnabledRemotely, false, OptionHandle::requestRemoteEnable);
    }

    @Test
    public void requestLocalEnableAndAgrees() throws Exception {
        validateRequestNegotiation(TelnetConstants.WILL, optionManager::receivedDo, OptionHandle::isEnabledLocally, true, OptionHandle::requestLocalEnable);
    }

    @Test
    public void requestLocalEnableAndDisagrees() throws Exception {
        validateRequestNegotiation(TelnetConstants.WILL, optionManager::receivedDont, OptionHandle::isEnabledLocally, false, OptionHandle::requestLocalEnable);
    }

    @Test
    public void respondToNotAllowedDo() throws Exception {
        respondNotAllowed(optionManager::receivedDo, TelnetConstants.WONT);
    }

    @Test
    public void respondToNotAllowedWill() throws Exception {
        respondNotAllowed(optionManager::receivedWill, TelnetConstants.DONT);
    }

    public void respondNotAllowed(IntConsumer requested, byte command) {
        context.checking(sentOptionCommand(command));
        requested.accept(OPTION_ID);
    }

    @Test
    public void respondToAllowedWill() throws Exception {
        context.checking(sentOptionCommand(TelnetConstants.DO));
        option().allowRemote();
        optionManager.receivedWill(OPTION_ID);
    }

    @Test
    public void respondToAllowedDo() throws Exception {
        context.checking(sentOptionCommand(TelnetConstants.WILL));
        option().allowLocal();
        optionManager.receivedDo(OPTION_ID);

    }

    private ExpectationBuilder sentOptionCommand(byte command) {
        return new Expectations() {{
            oneOf(output).accept(with(new SentCommandMatcher(command)));
        }};
    }

    public Options options() {
        return optionManager.options();
    }

    public OptionHandle option() {
        return options().option(OPTION_ID);
    }

    public void validateRequestNegotiation(byte command, IntConsumer reply, Predicate<OptionHandle> enabledChecker, boolean endsEnabled, Consumer<OptionHandle> request) {
        context.checking(sentOptionCommand(command));
        assertThat(enabledChecker.test(option()), equalTo(false));
        request.accept(option());
        assertThat(enabledChecker.test(option()), equalTo(false));
        reply.accept(OPTION_ID);
        assertThat(enabledChecker.test(option()), equalTo(endsEnabled));
    }

    private static class SentCommandMatcher extends BaseMatcher<ByteBuffer> {
        private final byte[] expected;

        public SentCommandMatcher(byte command) {
            expected = new byte[]{TelnetConstants.IAC, command, OPTION_ID};
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("ByteBuffer containing").appendValue(expected);
        }

        @Override
        public boolean matches(Object item) {
            if (!(item instanceof ByteBuffer)) {
                return false;
            }
            return Arrays.equals((byte[]) unwrap(item), expected);
        }

        public Object unwrap(Object item) {
            if (item instanceof ByteBuffer) {
                final ByteBuffer bb = (ByteBuffer) item;
                bb.mark();
                final byte[] bytes = new byte[bb.remaining()];
                bb.get(bytes);
                bb.reset();
                return bytes;
            }
            return item;
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            super.describeMismatch(unwrap(item), description);
        }
    }
}