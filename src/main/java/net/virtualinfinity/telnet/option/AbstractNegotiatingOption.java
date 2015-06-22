package net.virtualinfinity.telnet.option;

import net.virtualinfinity.telnet.Option;
import net.virtualinfinity.telnet.OptionHandle;
import net.virtualinfinity.telnet.Session;
import net.virtualinfinity.telnet.SubNegotiationOutputChannel;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
abstract class AbstractNegotiatingOption {
    protected final OptionHandle optionHandle;
    private final SubNegotiationOutputChannel outputChannel;
    protected static <T extends AbstractNegotiatingOption> T create(Function<Session, T> item, Session session) {
        final T result = item.apply(session);
        doAttach(result);
        return result;
    }

    /**
     * This method is used to help silence the compiler.  Don't believe me? Try to inline it.
     * @param ano
     */
    private static void doAttach(AbstractNegotiatingOption ano) {
        ano.attach();
    }

    protected AbstractNegotiatingOption(Session session, Option option) {
        this.optionHandle = session.options().option(option);
        this.outputChannel = session.subNegotiationOutputChannel();
    }

    private void attach() {
        final OptionStateListener stateListener = getStateListener();
        if (stateListener != null) {
            optionHandle.addStateListener(stateListener);
        }
        final SubNegotiationListener negotiationListener = getNegotiationListener();
        if (negotiationListener != null) {
            optionHandle.setSubNegotiationListener(negotiationListener);
        }
    }

    protected void sendSubNegotiation(ByteBuffer data) {
        outputChannel.sendSubNegotiation(optionHandle.optionCode(), data);
    }

    protected abstract SubNegotiationListener getNegotiationListener();

    protected abstract OptionStateListener getStateListener();

}
