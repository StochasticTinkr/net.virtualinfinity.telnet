package net.virtualinfinity.telnet;

import net.virtualinfinity.telnet.option.OptionStateListener;
import net.virtualinfinity.telnet.option.SubNegotiationListener;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;

/**
 * An implementation of the OptionCommandManager.
 *
 * @author Daniel Pitts
 */
class OptionCommandManagerImpl implements OptionCommandManager {
    private static final Logger logger = Logger.getLogger(OptionCommandManagerImpl.class);
    private final Consumer<ByteBuffer> output;
    private final OptionState[] optionStates = new OptionState[256];
    private final Map<Integer, List<OptionStateListener>> optionStateListeners = new HashMap<>();
    private final Map<Integer, SubNegotiationListener> subNegotiationListeners = new HashMap<>();

    OptionCommandManagerImpl(Consumer<ByteBuffer> output) {
        this.output = output;
    }

    private void enabledLocally(int optionId) {
        optionStateListeners.getOrDefault(optionId, Collections.emptyList()).forEach(OptionStateListener::enabledLocally);
    }
    private void disabledLocally(int optionId) {
        optionStateListeners.getOrDefault(optionId, Collections.emptyList()).forEach(OptionStateListener::disabledLocally);
    }

    private OptionState optionState(int optionId) {
        OptionState optionState = optionStates[optionId&255];
        if (optionState == null) {
            optionState = new OptionState();
            optionStates[optionId] = optionState;
        }
        return optionState;
    }


    private void enabledRemotely(int optionId) {
        optionStateListeners.getOrDefault(optionId, Collections.emptyList()).forEach(OptionStateListener::enabledRemotely);
    }

    private void disabledRemotely(int optionId) {
        optionStateListeners.getOrDefault(optionId, Collections.emptyList()).forEach(OptionStateListener::disabledRemotely);
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

    @Override
    public SubNegotiationListener getSubNegotiationListener(int optionId) {
        return subNegotiationListeners.get(optionId);
    }

    SubNegotiationOutputChannel subNegotiationOutputChannel(OutputChannel outputChannel) {
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
                return new OptionHandleImpl(hasOptionCode, OptionCommandManagerImpl.this);
            }

            @Override
            public OptionHandle option(int optionCode) {
                return option(() -> optionCode);
            }

        };
    }

    private void updateOptionState(HasOptionCode option, Function<OptionState, ObjIntConsumer<OptionCommandManagerImpl>> command) {
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

    private Collection<OptionStateListener> stateListenerList(int optionCode) {
        return optionStateListeners.computeIfAbsent(optionCode, integer -> new ArrayList<>());
    }

    private static class OptionHandleImpl implements OptionHandle {

        private final HasOptionCode option;
        private final OptionCommandManagerImpl optionManager;
        private OptionHandleImpl(HasOptionCode option, OptionCommandManagerImpl OptionManagerImpl) {
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

        @Override
        public OptionHandle addStateListener(OptionStateListener optionStateListener) {
            optionManager.stateListenerList(optionCode()).add(optionStateListener);
            return this;
        }

        @Override
        public OptionHandle removeStateListener(OptionStateListener optionStateListener) {
            optionManager.stateListenerList(optionCode()).remove(optionStateListener);
            return this;
        }

        @Override
        public OptionHandle setSubNegotiationListener(SubNegotiationListener subNegotiationListener) {
            optionManager.subNegotiationListeners.put(optionCode(), subNegotiationListener);
            return this;
        }

        @Override
        public int optionCode() {
            return option.optionCode();
        }
    }

    enum Response implements ObjIntConsumer<OptionCommandManagerImpl> {
        SEND_WONT(OptionCommandManagerImpl::sendWont),
        IS_ENABLED_LOCALLY(OptionCommandManagerImpl::enabledLocally),
        IS_DISABLED_LOCALLY(OptionCommandManagerImpl::disabledLocally),
        NO_RESPONSE((OptionManagerImpl, value) -> {}),
        SEND_DO(OptionCommandManagerImpl::sendDo),
        IS_ENABLED_REMOTELY(OptionCommandManagerImpl::enabledRemotely),
        IS_DISABLED_REMOTELY(OptionCommandManagerImpl::disabledRemotely),
        SEND_DONT(OptionCommandManagerImpl::sendDont),
        SEND_WILL(OptionCommandManagerImpl::sendWill),
        ;

        private final ObjIntConsumer<OptionCommandManagerImpl> responder;

        Response(ObjIntConsumer<OptionCommandManagerImpl> responder) {
            this.responder = responder;
        }

        public void accept(OptionCommandManagerImpl OptionManagerImpl, int optionId) {
            responder.accept(OptionManagerImpl, optionId);
        }

        public ObjIntConsumer<OptionCommandManagerImpl> and(ObjIntConsumer<OptionCommandManagerImpl> otherResponse) {
            return (OptionManagerImpl, value) -> {
                accept(OptionManagerImpl, value);
                otherResponse.accept(OptionManagerImpl, value);
            };
        }
    }


    private static final class SubNegotiationOutputChannelImpl implements SubNegotiationOutputChannel {
        private final OptionCommandManagerImpl optionCommandManagerImpl;
        private final OutputChannel outputChannel;

        public SubNegotiationOutputChannelImpl(OptionCommandManagerImpl optionCommandManagerImpl, OutputChannel outputChannel) {
            this.optionCommandManagerImpl = optionCommandManagerImpl;
            this.outputChannel = outputChannel;
        }

        @Override
        public void sendSubNegotiation(int optionId, ByteBuffer data) {
            optionCommandManagerImpl.sendOptionCommand(TelnetConstants.SB, (byte) optionId);
            outputChannel.write(data);
            optionCommandManagerImpl.output.accept(ByteBuffer.wrap(new byte[]{TelnetConstants.IAC, TelnetConstants.SE}));
        }
    }
}
