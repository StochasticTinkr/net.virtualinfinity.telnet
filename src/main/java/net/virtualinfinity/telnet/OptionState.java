package net.virtualinfinity.telnet;

import java.util.function.Function;
import java.util.function.ObjIntConsumer;

/**
 * Manages the state of an option.
 *
 * @author Daniel Pitts
 */
class OptionState {
    /**
     * The state at one end of the connection.
     */
    private static class EndState
    {
        /**
         * Remote sent "DO" or "WILL"
         */
        private boolean remoteWants;

        /**
         * Local replied "DO" or "WILL
         */
        private boolean localWants;

        /**
         * Actual state of option.
         */
        private boolean enabled;

        /**
         * Respond affirmatively if asked.
         */
        private boolean supported;

        /**
         * @return true if both sides agree to enable the feature.
         */
        public boolean isEnabled() {
            return enabled;
        }

        public void remoteWants() {
            remoteWants = true;
        }

        public void localWants() {
            this.localWants = true;
        }

        public boolean isSupported() {
            return supported;
        }

        public EndState allow() {
            this.supported = true;
            return this;
        }

    }

    /**
     * The responses to send in the case of a state change.
     */
    private static class Responses {
        private final Function<OptionState, EndState> endGetter;
        private final OptionCommandManagerImpl.Response requestDisable;
        private final ObjIntConsumer<OptionCommandManagerImpl> enabled;
        private final ObjIntConsumer<OptionCommandManagerImpl> disabled;
        private final ObjIntConsumer<OptionCommandManagerImpl> requestEnable;

        /**
         *
         * @param endGetter Supplier of the "end" this is for.
         * @param requestDisable The response that causes us to send a disable request (WON'T, DON'T).
         * @param requestEnable The response that causes us to send an enable request (WILL, DO)
         * @param enabled The response that causes us to dispatch an "Enabled" event to the appropriate listeners.
         * @param disabled The response that causes us to dispatch an "Disabled" event to the appropriate listeners.
         */
        public Responses(Function<OptionState, EndState> endGetter, OptionCommandManagerImpl.Response requestDisable, ObjIntConsumer<OptionCommandManagerImpl> requestEnable, ObjIntConsumer<OptionCommandManagerImpl> enabled, ObjIntConsumer<OptionCommandManagerImpl> disabled) {
            this.endGetter = endGetter;
            this.requestDisable = requestDisable;
            this.requestEnable = requestEnable;
            this.enabled = enabled;
            this.disabled = disabled;
        }

        /**
         * Returns the action that should be performed when the remote disables the given option.
         *
         * @param state the option state.
         *
         * @return the action to perform.
         */
        public ObjIntConsumer<OptionCommandManagerImpl> remoteDisables(OptionState state) {
            final EndState end = endGetter.apply(state);
            end.remoteWants = false;
            if (end.isEnabled()) {
                if (end.localWants) {
                    return requestDisable.and(disabled(end));
                } else {
                    return disabled(end);
                }
            }
            return OptionCommandManagerImpl.Response.NO_RESPONSE;
        }

        /**
         * Returns the action that should be performed when the local side disables the given option.
         *
         * @param state the option state.
         *
         * @return the action to perform.
         */
        public ObjIntConsumer<OptionCommandManagerImpl> localDisables(OptionState state) {
            final EndState end = endGetter.apply(state);
            end.localWants = false;
            if (end.remoteWants) {
                return requestDisable;
            }
            return disabled(end);
        }

        /**
         * Returns the action that should be performed when the option is disabled.
         *
         * @param end the option end that this applies to.
         *
         * @return the action to perform.
         */
        public ObjIntConsumer<OptionCommandManagerImpl> disabled(EndState end) {
            end.enabled = false;
            return disabled;
        }

        /**
         * Returns the action that should be performed when the local side requests the given option.
         *
         * @param state the option state.
         *
         * @return the action to perform.
         */
        public ObjIntConsumer<OptionCommandManagerImpl> localWants(OptionState state) {
            final EndState end = endGetter.apply(state);
            end.allow(); // Force allowed, since we're advertising it.
            end.localWants();
            if (end.remoteWants) {
                return enabled(end);
            }
            return requestEnable;
        }

        /**
         * Returns the action that should be performed when the option is enabled.
         *
         * @param end the option end that this applies to.
         *
         * @return the action to perform.
         */
        public ObjIntConsumer<OptionCommandManagerImpl> enabled(EndState end) {
            end.enabled = true;
            return enabled;
        }

        /**
         * Returns the action that should be performed when the remote side wants the given option.
         *
         * @param state the option state.
         *
         * @return the action to perform.
         */
        public ObjIntConsumer<OptionCommandManagerImpl> remoteWants(OptionState state) {
            final EndState end = endGetter.apply(state);
            if (!end.isSupported()) {
                return requestDisable;
            }
            end.remoteWants();
            if (end.localWants) {
                if (end.isEnabled()) {
                    return OptionCommandManagerImpl.Response.NO_RESPONSE; // no change.
                }
                return enabled(end);
            }
            return requestEnable;
        }
    }

    /**
     * Responses to requests for local options.
     */
    private static final Responses localResponse =
        new Responses(OptionState::local, OptionCommandManagerImpl.Response.SEND_WONT, OptionCommandManagerImpl.Response.SEND_WILL,
            OptionCommandManagerImpl.Response.IS_ENABLED_LOCALLY, OptionCommandManagerImpl.Response.IS_DISABLED_LOCALLY);

    /**
     * Responses to requests for remote options.
     */
    private static final Responses remoteResponse =
        new Responses(OptionState::remote, OptionCommandManagerImpl.Response.SEND_DONT, OptionCommandManagerImpl.Response.SEND_DO,
            OptionCommandManagerImpl.Response.IS_ENABLED_REMOTELY, OptionCommandManagerImpl.Response.IS_DISABLED_REMOTELY);

    /**
     * The state of the option on the remote end.
     */
    private final EndState remote = new EndState();

    /**
     * The state of the option on the local end.
     */
    private final EndState local = new EndState();

    public EndState remote() {
        return remote;
    }

    public EndState local() {
        return local;
    }

    /**
     * @return the action to perform when we receive a DO.
     */
    public ObjIntConsumer<OptionCommandManagerImpl> receivedDo() {
        return localResponse.remoteWants(this);
    }

    /**
     * @return the action to perform when we receive a WILL.
     */
    public ObjIntConsumer<OptionCommandManagerImpl> receivedWill() {
        return remoteResponse.remoteWants(this);
    }

    /**
     * @return the action to perform when we receive a DON'T.
     */
    public ObjIntConsumer<OptionCommandManagerImpl> receivedDont() {
        return localResponse.remoteDisables(this);
    }

    /**
     * @return the action to perform when we receive a WON'T.
     */
    public ObjIntConsumer<OptionCommandManagerImpl> receivedWont() {
        return remoteResponse.remoteDisables(this);
    }

    /**
     * @return the action to perform when we want to enable the option locally.
     */
    public ObjIntConsumer<OptionCommandManagerImpl> enableLocal() {
        return localResponse.localWants(this);
    }

    /**
     * @return the action to perform when we want to enable the option remotely.
     */
    public ObjIntConsumer<OptionCommandManagerImpl> enableRemote() {
        return remoteResponse.localWants(this);
    }

    /**
     * @return the action to perform when we want to disable the option locally.
     */
    public ObjIntConsumer<OptionCommandManagerImpl> disableLocal() {
        return localResponse.localDisables(this);
    }

    /**
     * @return the action to perform when we want to enable the option remotely.
     */
    public ObjIntConsumer<OptionCommandManagerImpl> disableRemote() {
        return remoteResponse.localDisables(this);
    }

    /**
     * Mark the remote option as being allowed. Future suggestions by the remote side that they enable the option will
     * be responded with positively.
     */
    public void allowRemote() {
        remote().allow();
    }

    /**
     * Mark the local option as being allowed. Future suggestions by the remote side that we enable the option will
     * be responded with positively.
     */
    public void allowLocal() {
        local().allow();
    }

    /**
     * @return true if there is agreement that this option is enabled on the remote side.
     */
    public boolean isEnabledRemotely() {
        return remote().isEnabled();
    }

    /**
     * @return true if there is agreement that this option is enabled on the local side.
     */
    public boolean isEnabledLocally() {
        return local().isEnabled();
    }

    @Override
    public String toString() {
        return "{" + (remote().localWants ? "DO" : "DON'T") + "->" +
                (remote().remoteWants ? "WILL" : "WON'T") +
                "," +
                (local().remoteWants ? "WILL" : "WON'T") + "->" +
                (local().localWants ? "DO" : "DON'T") +
                "}";
    }
}
