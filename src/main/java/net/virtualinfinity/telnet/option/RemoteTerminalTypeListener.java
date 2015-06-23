package net.virtualinfinity.telnet.option;

/**
 * A listener called when the remote side reports a new terminal type.
 * @see TerminalType
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface RemoteTerminalTypeListener {
    void terminalSet(String terminalName);
}
