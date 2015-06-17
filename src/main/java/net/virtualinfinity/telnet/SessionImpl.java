package net.virtualinfinity.telnet;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
final class SessionImpl implements Session {
    private final Options options;
    private final OutputChannel outputChannel;

    SessionImpl(Options options, OutputChannel outputChannel) {
        this.options = options;
        this.outputChannel = outputChannel;
    }

    @Override
    public Options options() {
        return options;
    }

    @Override
    public OutputChannel outputChannel() {
        return outputChannel;
    }
}
