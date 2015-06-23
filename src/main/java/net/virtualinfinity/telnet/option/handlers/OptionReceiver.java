package net.virtualinfinity.telnet.option.handlers;

import net.virtualinfinity.telnet.HasOptionCode;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
@Deprecated
public interface OptionReceiver<T> extends HasOptionCode {
    /**
     * Called when both DO was requested and WILL was received.
     *
     * @param handlerData option handler specific, unique to the session.
     *
     * @return A new value for the handlerData
     */
    T enabledRemotely(T handlerData);

    /**
     * Called when either DON'T was requested, or WON'T was received.
     *
     * @param handlerData option handler specific data, unique to the session.
     *
     * @return A new value for the handlerData
     */
    T disabledRemotely(T handlerData);

    /**
     * Called when both WILL was requested and DO was received.
     *
     * @param handlerData option handler specific, unique to the session.
     *
     * @return A new value for the handlerData
     */
    T enabledLocally(T handlerData);

    /**
     * Called when either WON'T was requested, or DON'T was received.
     *
     * @param handlerData option handler specific data, unique to the session.
     *
     * @return A new value for the handlerData
     */
    T disabledLocally(T handlerData);
}
