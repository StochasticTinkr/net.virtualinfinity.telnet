package net.virtualinfinity.telnet.option;

/**
 * A listener called when the remote side reports a new terminal type.
 * @see TerminalType
 * @author Daniel Pitts
 */
public interface RemoteTerminalTypeListener {
    void terminalSet(String terminalName);
}
