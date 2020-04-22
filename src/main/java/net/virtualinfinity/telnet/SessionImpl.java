package net.virtualinfinity.telnet;

import java.io.Closeable;
import java.io.IOException;

/**
 * An implementation of the Session Facade.
 *
 * @author Daniel Pitts
 */
final class SessionImpl implements Session {
    private final Options options;
    private final OutputChannel outputChannel;
    private final SubNegotiationOutputChannel subNegotiationOutputChannel;
    private final Closeable closer;

    /**
     * Constructs a new Session facade.
     * @param options the options instance.
     * @param outputChannel the output channel
     * @param subNegotiationOutputChannel the sub-negotiation output channel
     * @param closer the action to perform on close.
     */
    SessionImpl(Options options, OutputChannel outputChannel, SubNegotiationOutputChannel subNegotiationOutputChannel, Closeable closer) {
        this.options = options;
        this.outputChannel = outputChannel;
        this.subNegotiationOutputChannel = subNegotiationOutputChannel;
        this.closer = closer;
    }

    @Override
    public Options options() {
        return options;
    }

    @Override
    public OutputChannel outputChannel() {
        return outputChannel;
    }

    @Override
    public SubNegotiationOutputChannel subNegotiationOutputChannel() {
        return subNegotiationOutputChannel;
    }

    @Override
    public void close() throws IOException {
        closer.close();
    }
}
