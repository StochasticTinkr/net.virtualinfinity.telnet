package net.virtualinfinity.telnet;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
final class SessionImpl implements Session {
    private final Options options;
    private final OutputChannel outputChannel;
    private final SubNegotationOutputChannel subNegotationOutputChannel;
    private final Closeable closer;

    SessionImpl(Options options, OutputChannel outputChannel, SubNegotationOutputChannel subNegotationOutputChannel, Closeable closer) {
        this.options = options;
        this.outputChannel = outputChannel;
        this.subNegotationOutputChannel = subNegotationOutputChannel;
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
    public SubNegotationOutputChannel subNegotationOutputChannel() {
        return subNegotationOutputChannel;
    }

    @Override
    public void close() throws IOException {
        closer.close();
    }
}
