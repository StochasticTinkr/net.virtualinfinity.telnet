package net.virtualinfinity.telnet;

/**
 * Provides factory methods for getting SessionStarter instances.
 *
 *
 * The difference between "client" and "server" is whether or not all output data must be refreshed before the input
 * buffer is read from.
 * <p>
 * Use {@link #server()} or {@link #server(int)} for servers they may be subject to DoS attacks. This prevents reading
 * from the remote end while there is output still to process.
 * Without this protection, an attacker could send to the server a message which causes a large reply in the output, but
 * fail to read the result, causing buffers to fill up.
 * Since this is a non-blocking implementation, output buffers can grow unbounded, eventually causing an OOM.
 *
 * <p>
 * Use {@link #client()} or {@link #client(int)} for clients since they are generally user facing, and we expect the remote end to read as much
 * as is given to them, and also that our local user isn't trying to overflow his own buffers.
 * @author Daniel Pitts
 */
public final class SessionStarters {

    private static final int DEFAULT_INPUT_BUFFER_SIZE = 2048;

    /**
     * Creates a new session starter appropriate for client connections, using the default input buffer size of 2048.
     *
     * @return a SessionStarter.
     */
    public static SessionStarter client() {
        return client(DEFAULT_INPUT_BUFFER_SIZE);
    }

    /**
     * Creates a new session starter appropriate for client connections, using the given input buffer size.
     *
     * @param inputBufferSize The input buffer size.
     *
     * @return a SessionStarter.
     */
    public static SessionStarter client(int inputBufferSize) {
        return new DefaultSessionStarter(inputBufferSize, false);
    }

    /**
     * Creates a new session starter appropriate for server connections, using the default input buffer size of 2048.
     *
     * @return a SessionStarter.
     */
    public static SessionStarter server() {
        return server(DEFAULT_INPUT_BUFFER_SIZE);
    }

    /**
     * Creates a new session starter appropriate for server connections, using the given input buffer size.
     *
     * @param inputBufferSize The input buffer size.
     *
     * @return a SessionStarter.
     */
    public static SessionStarter server(int inputBufferSize) {
        return new DefaultSessionStarter(inputBufferSize, true);
    }
}
