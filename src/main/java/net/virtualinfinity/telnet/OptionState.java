package net.virtualinfinity.telnet;

import java.util.function.Function;
import java.util.function.ObjIntConsumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class OptionState {
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
        private final TelnetSession.Response requestDisable;
        private final ObjIntConsumer<TelnetSession> enabled;
        private final ObjIntConsumer<TelnetSession> disabled;
        private final TelnetSession.Response requestEnable;

        public Responses(Function<OptionState, EndState> endGetter, TelnetSession.Response requestDisable, TelnetSession.Response requestEnable, ObjIntConsumer<TelnetSession> enabled, ObjIntConsumer<TelnetSession> disabled) {
            this.endGetter = endGetter;
            this.requestDisable = requestDisable;
            this.requestEnable = requestEnable;
            this.enabled = enabled;
            this.disabled = disabled;
        }

        public ObjIntConsumer<TelnetSession> remoteDisables(OptionState state) {
            final EndState end = endGetter.apply(state);
            end.remoteWants = false;
            if (end.isEnabled()) {
                if (end.localWants) {
                    return requestDisable.and(disabled(end));
                } else {
                    return disabled(end);
                }
            }
            return TelnetSession.Response.NO_RESPONSE;
        }

        public ObjIntConsumer<TelnetSession> localDisables(OptionState state) {
            final EndState end = endGetter.apply(state);
            end.localWants = false;
            if (end.remoteWants) {
                return requestDisable;
            }
            return disabled(end);
        }

        public ObjIntConsumer<TelnetSession> disabled(EndState end) {
            end.enabled = false;
            return disabled;
        }

        public ObjIntConsumer<TelnetSession> localWants(OptionState state) {
            final EndState end = endGetter.apply(state);
            end.allow(); // Force allowed, since we're advertising it.
            end.localWants();
            if (end.remoteWants) {
                return enabled(end);
            }
            return requestEnable;
        }

        public ObjIntConsumer<TelnetSession> enabled(EndState end) {
            end.enabled = true;
            return enabled;
        }

        public ObjIntConsumer<TelnetSession> remoteWants(OptionState state) {
            final EndState end = endGetter.apply(state);
            if (!end.isSupported()) {
                return requestDisable;
            }
            end.remoteWants();
            if (end.localWants) {
                if (end.isEnabled()) {
                    return TelnetSession.Response.NO_RESPONSE; // no change.
                }
                return enabled(end);
            }
            return requestEnable;
        }
    }

    private static final Responses localResponse =
        new Responses(OptionState::local, TelnetSession.Response.SEND_WONT, TelnetSession.Response.SEND_WILL,
            TelnetSession.Response.IS_ENABLED_LOCALLY, TelnetSession.Response.IS_DISABLED_LOCALLY);

    private static final Responses remoteResponse =
        new Responses(OptionState::remote, TelnetSession.Response.SEND_DONT, TelnetSession.Response.SEND_DO,
            TelnetSession.Response.IS_ENABLED_REMOTELY, TelnetSession.Response.IS_DISABLED_REMOTELY);

    private final EndState remote = new EndState();
    private final EndState local = new EndState();

    public EndState remote() {
        return remote;
    }

    public EndState local() {
        return local;
    }
    public ObjIntConsumer<TelnetSession> receivedDo() {
        return localResponse.remoteWants(this);
    }

    public ObjIntConsumer<TelnetSession> receivedWill() {
        return remoteResponse.remoteWants(this);
    }

    public ObjIntConsumer<TelnetSession> receivedDont() {
        return localResponse.remoteDisables(this);
    }

    public ObjIntConsumer<TelnetSession> receivedWont() {
        return remoteResponse.remoteDisables(this);
    }

    public ObjIntConsumer<TelnetSession> enableLocal() {
        return localResponse.localWants(this);
    }

    public ObjIntConsumer<TelnetSession> enableRemote() {
        return remoteResponse.localWants(this);
    }

    public ObjIntConsumer<TelnetSession> disableLocal() {
        return localResponse.localDisables(this);
    }

    public ObjIntConsumer<TelnetSession> disableRemote() {
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
        StringBuilder builder = new StringBuilder("{");
        builder.append(remote().localWants ? "DO" : "DON'T").append("->");
        builder.append(remote().remoteWants ? "WILL" : "WON'T");
        builder.append(",");
        builder.append(local().remoteWants ? "WILL" : "WON'T").append("->");
        builder.append(local().localWants ? "DO" : "DON'T");

        return builder.append("}").toString();
    }
}
