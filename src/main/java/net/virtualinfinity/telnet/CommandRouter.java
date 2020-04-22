package net.virtualinfinity.telnet;

import java.nio.ByteBuffer;

/**
 * An implementation of the {@link CommandReceiver} interface which routes the commands to
 * other appropriate objects.
 *
 * @author Daniel Pitts
 */
class CommandRouter implements CommandReceiver {
    private final SessionListener listener;
    private final SubNegotiationDataRouter dataRouter;
    private final OptionCommandManager optionCommandManager;

    /**
     * Constructs a new CommandRouter that passes commands to the session listener, data router and option manager as needed.
     *
     * @param listener the session listener.
     * @param dataRouter teh data router.
     * @param optionCommandManager the option manager.
     */
    public CommandRouter(SessionListener listener, SubNegotiationDataRouter dataRouter, OptionCommandManager optionCommandManager) {
        this.listener = listener;
        this.dataRouter = dataRouter;
        this.optionCommandManager = optionCommandManager;
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
        dataRouter.receivedStartSubNegotiation(optionCommandManager.getSubNegotiationListener(optionId));
    }

    @Override
    public void receivedDo(int optionId) {
        optionCommandManager.receivedDo(optionId);
    }

    @Override
    public void receivedDont(int optionId) {
        optionCommandManager.receivedDont(optionId);
    }

    @Override
    public void receivedWill(int optionId) {
        optionCommandManager.receivedWill(optionId);
    }

    @Override
    public void receivedWont(int optionId) {
        optionCommandManager.receivedWont(optionId);
    }

}
