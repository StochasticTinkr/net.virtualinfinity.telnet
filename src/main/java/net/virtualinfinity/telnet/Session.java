package net.virtualinfinity.telnet;

import net.virtualinfinity.nio.EventLoop;

import java.io.Closeable;

/**
 * Provides access to the public aspects of the telnet session.
 *
 * @see SessionListener
 * @see ClientStarter#connect(EventLoop, String, SessionListener)
 * @see ClientStarter#connect(EventLoop, String, int, SessionListener)
 *
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface Session extends Closeable {
    /**
     * @return an helper for managing the state of options on this session.
     */
    Options options();

    /**
     * @return the output data stream.
     */
    OutputChannel outputChannel();

    /**
     * @return the SubNegotiationOutputChannel for option sub-negotiation.
     */
    SubNegotiationOutputChannel subNegotiationOutputChannel();
}
