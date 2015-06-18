package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;

import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
class CommandRouter implements CommandReceiver {
    private final SessionListener listener;
    private final SubNegotiationDataRouter dataRouter;
    private final OptionManager optionManager;

    public CommandRouter(SessionListener listener, SubNegotiationDataRouter dataRouter, OptionManager optionManager) {
        this.listener = listener;
        this.dataRouter = dataRouter;
        this.optionManager = optionManager;
    }

    @Override
    public void receivedBreak() {
        listener.doBreak();
    }

    @Override
    public void receivedInterrupt() {
        listener.doInterrupt();
    }

    @Override
    public void receivedAbortOutput() {
        listener.doAbortOutput();
    }

    @Override
    public void receivedAreYouThere() {
        listener.doAreYouThere();
    }

    @Override
    public void receivedEraseCharacter() {
        listener.doEraseCharacter();
    }

    @Override
    public void receivedEraseLine() {
        listener.doEraseLine();

    }

    @Override
    public void receivedGoAhead() {
        listener.doGoAhead();
    }

    @Override
    public void receivedEndSubNegotiation() {
        dataRouter.receivedEndSubNegotiation();
    }

    @Override
    public void receivedData(ByteBuffer bytes) {
        dataRouter.receivedData(bytes);
    }

    @Override
    public void receivedIAC() {
        dataRouter.receivedIAC();
    }

    @Override
    public void receivedStartSubNegotiation(int optionId) {
        final OptionSessionHandler<?> handler = optionManager.getSessionHandler(optionId);
        dataRouter.receivedStartSubNegotiation(handler);
    }

    @Override
    public void receivedDo(int optionId) {
        optionManager.receivedDo(optionId);
    }

    @Override
    public void receivedDont(int optionId) {
        optionManager.receivedDont(optionId);
    }

    @Override
    public void receivedWill(int optionId) {
        optionManager.receivedWill(optionId);
    }

    @Override
    public void receivedWont(int optionId) {
        optionManager.receivedWont(optionId);
    }

}
