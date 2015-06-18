package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.handlers.OptionReceiver;
import net.virtualinfinity.telnet.option.handlers.OptionSessionHandler;
import net.virtualinfinity.telnet.option.handlers.OptionSessionHandlerImpl;
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
class OptionManagerImpl implements OptionManager {
    private static final Logger logger = Logger.getLogger(OptionManagerImpl.class);
    private final Consumer<ByteBuffer> output;
    private final OptionState[] optionStates = new OptionState[256];
    private final Map<Integer, OptionSessionHandler<?>> optionSessionHandler = new HashMap<>();

    OptionManagerImpl(Consumer<ByteBuffer> output) {
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

    @Override
    public void receivedDo(int optionId) {
        logOptionCommand("Received DO", optionId);
        optionState(optionId).receivedDo().accept(this, optionId);
    }

    @Override
    public void receivedDont(int optionId) {
        logOptionCommand("Received DON'T", optionId);
        optionState(optionId).receivedDont().accept(this, optionId);
    }

    @Override
    public void receivedWill(int optionId) {
        logOptionCommand("Received WILL", optionId);
        optionState(optionId).receivedWill().accept(this, optionId);
    }

    @Override
    public void receivedWont(int optionId) {
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

    public OptionSessionHandler<?> getSessionHandler(int optionId) {
        return optionSessionHandler.get(optionId);
    }

    SubNegotiationOutputChannel subNegotitationOutputChannel(OutputChannel outputChannel) {
        return new SubNegotiationOutputChannelImpl(this, outputChannel);

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
                return new OptionHandleImpl(hasOptionCode, OptionManagerImpl.this);
            }

            @Override
            public OptionHandle option(int optionCode) {
                return option(() -> optionCode);
            }

            @Override
            public <T, R extends SubNegotiationReceiver<T> & OptionReceiver<T>> OptionHandle installOptionReceiver(R receiver) {
                optionSessionHandler.put(receiver.optionCode(), OptionSessionHandlerImpl.of(receiver));
                return option(receiver);
            }
        };
    }

    private void updateOptionState(HasOptionCode option, Function<OptionState, ObjIntConsumer<OptionManagerImpl>> command) {
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
        private final OptionManagerImpl optionManager;
        private OptionHandleImpl(HasOptionCode option, OptionManagerImpl OptionManagerImpl) {
            this.option = option;
            this.optionManager = OptionManagerImpl;
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

    enum Response implements ObjIntConsumer<OptionManagerImpl> {
        SEND_WONT(OptionManagerImpl::sendWont),
        IS_ENABLED_LOCALLY(OptionManagerImpl::enabledLocally),
        IS_DISABLED_LOCALLY(OptionManagerImpl::disabledLocally),
        NO_RESPONSE((OptionManagerImpl, value) -> {}),
        SEND_DO(OptionManagerImpl::sendDo),
        IS_ENABLED_REMOTELY(OptionManagerImpl::enabledRemotely),
        IS_DISABLED_REMOTELY(OptionManagerImpl::disabledRemotely),
        SEND_DONT(OptionManagerImpl::sendDont),
        SEND_WILL(OptionManagerImpl::sendWill),
        ;

        private final ObjIntConsumer<OptionManagerImpl> responder;

        Response(ObjIntConsumer<OptionManagerImpl> responder) {
            this.responder = responder;
        }

        public void accept(OptionManagerImpl OptionManagerImpl, int optionId) {
            responder.accept(OptionManagerImpl, optionId);
        }

        public ObjIntConsumer<OptionManagerImpl> and(ObjIntConsumer<OptionManagerImpl> otherResponse) {
            return (OptionManagerImpl, value) -> {
                accept(OptionManagerImpl, value);
                otherResponse.accept(OptionManagerImpl, value);
            };
        }
    }


    private final class SubNegotiationOutputChannelImpl implements SubNegotiationOutputChannel {
        private final OptionManagerImpl OptionManagerImpl;
        private final OutputChannel outputChannel;

        public SubNegotiationOutputChannelImpl(OptionManagerImpl OptionManagerImpl, OutputChannel outputChannel) {
            this.OptionManagerImpl = OptionManagerImpl;
            this.outputChannel = outputChannel;
        }

        @Override
        public void sendSubNegotiation(int optionId, ByteBuffer data) {
            OptionManagerImpl.sendOptionCommand(TelnetConstants.SB, (byte) optionId);
            outputChannel.write(data);
            OptionManagerImpl.output.accept(ByteBuffer.wrap(new byte[]{TelnetConstants.IAC, TelnetConstants.SE}));
        }
    }
}
