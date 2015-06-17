package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.handlers.OptionReceiver;
import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;
import net.virtualinfinity.telnet.option.handlers.SubNegotiationReceiver;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
class OptionManager {
    private static final Logger logger = Logger.getLogger(OptionManager.class);
    private final Consumer<ByteBuffer> output;
    private final OptionState[] optionStates = new OptionState[256];
    private final Map<Integer, OptionSessionHandler<?>> optionSessionHandler = new HashMap<>();

    OptionManager(Consumer<ByteBuffer> output) {
        this.output = output;
    }

    private void enabledLocally(int optionId) {
        dispatchToRemoteHandler(optionId, OptionSessionHandler::enabledLocally);
    }
    private void disabledLocally(int optionId) {
        dispatchToRemoteHandler(optionId, OptionSessionHandler::disabledLocally);
    }

    private OptionState optionState(int optionId) {
        OptionState optionState = optionStates[optionId&255];
        if (optionState == null) {
            optionState = new OptionState();
            optionStates[optionId] = optionState;
        }
        return optionState;
    }

    private void dispatchToRemoteHandler(int optionId, Consumer<OptionSessionHandler<?>> command) {
        final OptionSessionHandler<?> handler = optionSessionHandler.get(optionId);
        if (handler != null) {
            command.accept(handler);
        }
    }

    private void enabledRemotely(int optionId) {
        dispatchToRemoteHandler(optionId, OptionSessionHandler::enabledRemotely);
    }

    private void disabledRemotely(int optionId) {
        dispatchToRemoteHandler(optionId, OptionSessionHandler::disabledRemotely);
    }

    private void sendDo(int optionId) {
        logOptionCommand("Sent DO", optionId);
        sendOptionCommand(TelnetConstants.DO, (byte) optionId);
    }

    private void sendDont(int optionId) {
        logOptionCommand("Sent DON'T", optionId);
        sendOptionCommand(TelnetConstants.DONT, (byte) optionId);
    }

    private void sendWill(int optionId) {
        logOptionCommand("Sent WILL", optionId);
        sendOptionCommand(TelnetConstants.WILL, (byte) optionId);
    }

    private void sendWont(int optionId) {
        logOptionCommand("Sent WONT", optionId);
        sendOptionCommand(TelnetConstants.WONT, (byte) optionId);
    }

    void receivedDo(int optionId) {
        logOptionCommand("Received DO", optionId);
        optionState(optionId).receivedDo().accept(this, optionId);
    }

    public void receivedDont(int optionId) {
        logOptionCommand("Received DON'T", optionId);
        optionState(optionId).receivedDont().accept(this, optionId);
    }

    void receivedWill(int optionId) {
        logOptionCommand("Received WILL", optionId);
        optionState(optionId).receivedWill().accept(this, optionId);
    }

    void receivedWont(int optionId) {
        logOptionCommand("Received WON'T", optionId);
        optionState(optionId).receivedWont().accept(this, optionId);
    }

    private void sendOptionCommand(byte command, byte optionId) {
        output.accept(ByteBuffer.wrap(new byte[]{TelnetConstants.IAC, command, optionId}));
    }

    private void logOptionCommand(String command, int optionId) {
        if (logger.isDebugEnabled()) {
            final Option option = Option.byId(optionId);
            logger.debug(command + ": " + option + " (" + optionId + ")");
        }
    }

    OptionSessionHandler<?> getSessionHandler(int optionId) {
        return optionSessionHandler.get(optionId);
    }

    SubNegotationOutputChannel subNegotitationOutputChannel(OutputChannel outputChannel) {
        return new SubNegotationOutputChannelImpl(this, outputChannel);

    }

    private OptionState optionState(HasOptionCode option) {
        return optionState(option.optionCode());
    }

    private void allowRemote(HasOptionCode option) {
        optionState(option).allowRemote();
    }

    private void allowLocal(HasOptionCode option) {
        optionState(option).allowLocal();
    }

    Options options() {
        return new Options() {
            @Override
            public OptionHandle option(HasOptionCode hasOptionCode) {
                return new OptionHandleImpl(hasOptionCode, OptionManager.this);
            }

            @Override
            public OptionHandle option(int optionCode) {
                return option(() -> optionCode);
            }

            @Override
            public <T, R extends SubNegotiationReceiver<T> & OptionReceiver<T>> OptionHandle installOptionReceiver(R receiver) {
                optionSessionHandler.put(receiver.optionCode(), OptionSessionHandler.of(receiver));
                return option(receiver);
            }
        };
    }

    private void updateOptionState(HasOptionCode option, Function<OptionState, ObjIntConsumer<OptionManager>> command) {
        command.apply(optionState(option.optionCode())).accept(this, option.optionCode());
    }
    private void requestRemoteEnable(HasOptionCode option) {
        updateOptionState(option, OptionState::enableRemote);
    }

    private void requestLocalEnable(HasOptionCode option) {
        updateOptionState(option, OptionState::enableLocal);
    }

    private void requestRemoteDisable(HasOptionCode option) {
        updateOptionState(option, OptionState::disableRemote);
    }

    private void requestLocalDisable(HasOptionCode option) {
        updateOptionState(option, OptionState::disableLocal);
    }

    private boolean isEnabledRemotely(HasOptionCode option) {
        return optionState(option).isEnabledRemotely();
    }

    private boolean isEnabledLocally(HasOptionCode option) {
        return optionState(option).isEnabledLocally();
    }

    private static class OptionHandleImpl implements OptionHandle {

        private final HasOptionCode option;
        private final OptionManager optionManager;
        private OptionHandleImpl(HasOptionCode option, OptionManager optionManager) {
            this.option = option;
            this.optionManager = optionManager;
        }

        @Override
        public boolean isEnabledRemotely() {
            return optionManager.isEnabledRemotely(option);
        }

        @Override
        public boolean isEnabledLocally() {
            return optionManager.isEnabledLocally(option);
        }

        @Override
        public OptionHandle requestRemoteEnable() {
            optionManager.requestRemoteEnable(option);

            return this;
        }

        @Override
        public OptionHandle requestLocalEnable() {
            optionManager.requestLocalEnable(option);

            return this;
        }

        @Override
        public OptionHandle requestRemoteDisable() {
            optionManager.requestRemoteDisable(option);

            return this;
        }

        @Override
        public OptionHandle requestLocalDisable() {
            optionManager.requestLocalDisable(option);

            return this;
        }

        @Override
        public OptionHandle allowLocal() {
            optionManager.allowLocal(option);

            return this;
        }

        @Override
        public OptionHandle allowRemote() {
            optionManager.allowRemote(option);

            return this;
        }

    }

    enum Response implements ObjIntConsumer<OptionManager> {
        SEND_WONT(OptionManager::sendWont),
        IS_ENABLED_LOCALLY(OptionManager::enabledLocally),
        IS_DISABLED_LOCALLY(OptionManager::disabledLocally),
        NO_RESPONSE((optionManager, value) -> {}),
        SEND_DO(OptionManager::sendDo),
        IS_ENABLED_REMOTELY(OptionManager::enabledRemotely),
        IS_DISABLED_REMOTELY(OptionManager::disabledRemotely),
        SEND_DONT(OptionManager::sendDont),
        SEND_WILL(OptionManager::sendWill),
        ;

        private final ObjIntConsumer<OptionManager> responder;

        Response(ObjIntConsumer<OptionManager> responder) {
            this.responder = responder;
        }

        public void accept(OptionManager optionManager, int optionId) {
            responder.accept(optionManager, optionId);
        }

        public ObjIntConsumer<OptionManager> and(ObjIntConsumer<OptionManager> otherResponse) {
            return (optionManager, value) -> {
                accept(optionManager, value);
                otherResponse.accept(optionManager, value);
            };
        }
    }


    private final class SubNegotationOutputChannelImpl implements SubNegotationOutputChannel {
        private final OptionManager optionManager;
        private final OutputChannel outputChannel;

        public SubNegotationOutputChannelImpl(OptionManager optionManager, OutputChannel outputChannel) {
            this.optionManager = optionManager;
            this.outputChannel = outputChannel;
        }

        @Override
        public void sendSubNegotiation(int optionId, ByteBuffer data) {
            optionManager.sendOptionCommand(TelnetConstants.SB, (byte) optionId);
            outputChannel.write(data);
            optionManager.output.accept(ByteBuffer.wrap(new byte[]{TelnetConstants.IAC, TelnetConstants.SE}));
        }
    }
}
