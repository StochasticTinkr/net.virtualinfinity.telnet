package net.virtualinfinity.telnet;

import java.io.Closeable;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface Session extends Closeable {
    Options options();
    OutputChannel outputChannel();
    SubNegotiationOutputChannel subNegotiationOutputChannel();
}
