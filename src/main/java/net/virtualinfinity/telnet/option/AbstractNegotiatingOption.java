package net.virtualinfinity.telnet.option;

import net.virtualinfinity.telnet.Option;
import net.virtualinfinity.telnet.OptionHandle;
import net.virtualinfinity.telnet.Session;
import net.virtualinfinity.telnet.SubNegotiationOutputChannel;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * This base class handles creating an attached option handler.
 *
 * @author Daniel Pitts
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
     *
     * @param ano the AbstractNegotiatingOption to attach.
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

    /**
     * @return the SubNegotiationListener that handles this option, or null if no sub-negotiation is needed.
     */
    protected abstract SubNegotiationListener getNegotiationListener();

    /**
     * @return the option state listener that handles this option, or null of no option-state listener is needed.
     */
    protected abstract OptionStateListener getStateListener();

}
