package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;

import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
class CommandRouter implements CommandReceiver {
    private final SessionListener listener;
    private final CommandDataRouter commandManager;
    private final OptionManager optionManager;

    public CommandRouter(SessionListener listener, CommandDataRouter commandManager, OptionManager optionManager) {
        this.listener = listener;
        this.commandManager = commandManager;
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
        commandManager.receivedEndSubNegotiation();
    }

    @Override
    public void receivedData(ByteBuffer bytes) {
        commandManager.receivedData(bytes);
    }

    @Override
    public void receivedIAC() {
        commandManager.receivedIAC();
    }

    @Override
    public void receivedStartSubNegotiation(int optionId) {
        final OptionSessionHandler<?> handler = optionManager.getSessionHandler(optionId);
        commandManager.receivedStartSubNegotiation(handler);
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
