package net.virtualinfinity.telnet;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface RemoteTerminalTypeListener {
    void terminalSet(String terminalName, TelnetSession session);
}