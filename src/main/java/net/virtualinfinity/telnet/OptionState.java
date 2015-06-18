package net.virtualinfinity.telnet;

import java.util.function.Function;
import java.util.function.ObjIntConsumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
class OptionState {
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

    private static class Responses {
        private final Function<OptionState, EndState> endGetter;
        private final OptionManagerImpl.Response requestDisable;
        private final ObjIntConsumer<OptionManagerImpl> enabled;
        private final ObjIntConsumer<OptionManagerImpl> disabled;
        private final ObjIntConsumer<OptionManagerImpl> requestEnable;

        public Responses(Function<OptionState, EndState> endGetter, OptionManagerImpl.Response requestDisable, ObjIntConsumer<OptionManagerImpl> requestEnable, ObjIntConsumer<OptionManagerImpl> enabled, ObjIntConsumer<OptionManagerImpl> disabled) {
            this.endGetter = endGetter;
            this.requestDisable = requestDisable;
            this.requestEnable = requestEnable;
            this.enabled = enabled;
            this.disabled = disabled;
        }

        public ObjIntConsumer<OptionManagerImpl> remoteDisables(OptionState state) {
            final EndState end = endGetter.apply(state);
            end.remoteWants = false;
            if (end.isEnabled()) {
                if (end.localWants) {
                    return requestDisable.and(disabled(end));
                } else {
                    return disabled(end);
                }
            }
            return OptionManagerImpl.Response.NO_RESPONSE;
        }

        public ObjIntConsumer<OptionManagerImpl> localDisables(OptionState state) {
            final EndState end = endGetter.apply(state);
            end.localWants = false;
            if (end.remoteWants) {
                return requestDisable;
            }
            return disabled(end);
        }

        public ObjIntConsumer<OptionManagerImpl> disabled(EndState end) {
            end.enabled = false;
            return disabled;
        }

        public ObjIntConsumer<OptionManagerImpl> localWants(OptionState state) {
            final EndState end = endGetter.apply(state);
            end.allow(); // Force allowed, since we're advertising it.
            end.localWants();
            if (end.remoteWants) {
                return enabled(end);
            }
            return requestEnable;
        }

        public ObjIntConsumer<OptionManagerImpl> enabled(EndState end) {
            end.enabled = true;
            return enabled;
        }

        public ObjIntConsumer<OptionManagerImpl> remoteWants(OptionState state) {
            final EndState end = endGetter.apply(state);
            if (!end.isSupported()) {
                return requestDisable;
            }
            end.remoteWants();
            if (end.localWants) {
                if (end.isEnabled()) {
                    return OptionManagerImpl.Response.NO_RESPONSE; // no change.
                }
                return enabled(end);
            }
            return requestEnable;
        }
    }

    private static final Responses localResponse =
        new Responses(OptionState::local, OptionManagerImpl.Response.SEND_WONT, OptionManagerImpl.Response.SEND_WILL,
            OptionManagerImpl.Response.IS_ENABLED_LOCALLY, OptionManagerImpl.Response.IS_DISABLED_LOCALLY);

    private static final Responses remoteResponse =
        new Responses(OptionState::remote, OptionManagerImpl.Response.SEND_DONT, OptionManagerImpl.Response.SEND_DO,
            OptionManagerImpl.Response.IS_ENABLED_REMOTELY, OptionManagerImpl.Response.IS_DISABLED_REMOTELY);

    private final EndState remote = new EndState();
    private final EndState local = new EndState();

    public EndState remote() {
        return remote;
    }

    public EndState local() {
        return local;
    }
    public ObjIntConsumer<OptionManagerImpl> receivedDo() {
        return localResponse.remoteWants(this);
    }

    public ObjIntConsumer<OptionManagerImpl> receivedWill() {
        return remoteResponse.remoteWants(this);
    }

    public ObjIntConsumer<OptionManagerImpl> receivedDont() {
        return localResponse.remoteDisables(this);
    }

    public ObjIntConsumer<OptionManagerImpl> receivedWont() {
        return remoteResponse.remoteDisables(this);
    }

    public ObjIntConsumer<OptionManagerImpl> enableLocal() {
        return localResponse.localWants(this);
    }

    public ObjIntConsumer<OptionManagerImpl> enableRemote() {
        return remoteResponse.localWants(this);
    }

    public ObjIntConsumer<OptionManagerImpl> disableLocal() {
        return localResponse.localDisables(this);
    }

    public ObjIntConsumer<OptionManagerImpl> disableRemote() {
        return remoteResponse.localDisables(this);
    }

    public void allowRemote() {
        remote().allow();
    }

    public void allowLocal() {
        local().allow();
    }

    public boolean isEnabledRemotely() {
        return remote().isEnabled();
    }
    public boolean isEnabledLocally() {
        return local().isEnabled();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("{");
        builder.append(remote().localWants ? "DO" : "DON'T").append("->");
        builder.append(remote().remoteWants ? "WILL" : "WON'T");
        builder.append(",");
        builder.append(local().remoteWants ? "WILL" : "WON'T").append("->");
        builder.append(local().localWants ? "DO" : "DON'T");

        return builder.append("}").toString();
    }
}
