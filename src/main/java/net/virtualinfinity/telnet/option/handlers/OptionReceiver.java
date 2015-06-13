package net.virtualinfinity.telnet.option.handlers;

import net.virtualinfinity.telnet.HasOptionCode;
import net.virtualinfinity.telnet.TelnetSession;

/**
 *
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface OptionReceiver<T> extends HasOptionCode {
    /**
     * Called when both DO was requested and WILL was received.
     *
     * @param session the session this option was enabled on.
     * @param handlerData option handler specific, unique to the session.
     *
     * @return A new value for the handlerData
     */
    T enabledRemotely(TelnetSession session, T handlerData);

    /**
     * Called when either DON'T was requested, or WON'T was received.
     *
     * @param session the session this option was disabled on.
     * @param handlerData option handler specific data, unique to the session.
     *
     * @return A new value for the handlerData
     */
    T disabledRemotely(TelnetSession session, T handlerData);

    /**
     * Called when both WILL was requested and DO was received.
     *
     * @param session the session this option was enabled on.
     * @param handlerData option handler specific, unique to the session.
     *
     * @return A new value for the handlerData
     */
    T enabledLocally(TelnetSession session, T handlerData);

    /**
     * Called when either WON'T was requested, or DON'T was received.
     *
     * @param session the session this option was disabled on.
     * @param handlerData option handler specific data, unique to the session.
     *
     * @return A new value for the handlerData
     */
    T disabledLocally(TelnetSession session, T handlerData);
}
