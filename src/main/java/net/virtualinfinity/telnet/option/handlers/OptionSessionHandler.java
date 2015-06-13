package net.virtualinfinity.telnet.option.handlers;

import net.virtualinfinity.telnet.TelnetSession;

import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public final class OptionSessionHandler<T> {
    private final SubNegotiationReceiver<T> subNegotiationReceiver;
    private final OptionReceiver<T> optionReceiver;
    private T sessionData;

    private interface ReceiverFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    public OptionSessionHandler(SubNegotiationReceiver<T> handler, OptionReceiver<T> optionReceiver) {
        this.subNegotiationReceiver = handler;
        this.optionReceiver = optionReceiver;
    }

    public static  <T, R extends SubNegotiationReceiver<T>&OptionReceiver<T>> OptionSessionHandler<T> of(R receiver) {
        return new OptionSessionHandler<>(receiver, receiver);
    }

    public static <T> OptionSessionHandler<T> of(OptionReceiver<T> receiver) {
        return new OptionSessionHandler<>(null, receiver);
    }
    public void enabledLocally(TelnetSession session) {
        apply(session, optionReceiver, OptionReceiver::enabledLocally);
    }

    public void disabledLocally(TelnetSession session) {
        apply(session, optionReceiver, OptionReceiver::disabledLocally);
    }
    public void enabledRemotely(TelnetSession session) {
        apply(session, optionReceiver, OptionReceiver::enabledRemotely);
    }

    public void disabledRemotely(TelnetSession session) {
        apply(session, optionReceiver, OptionReceiver::disabledRemotely);
    }

    private <R> void apply(TelnetSession session, R receiver, ReceiverFunction<R, TelnetSession, T, T> operation) {
        if (receiver == null) {
            return;
        }
        sessionData = operation.apply(receiver, session, sessionData);
    }

    public void endSubNegotiation(TelnetSession session) {
        apply(session, subNegotiationReceiver, SubNegotiationReceiver::endSubNegotiation);
    }

    public void startSubNegotiation(TelnetSession session) {
        apply(session, subNegotiationReceiver, SubNegotiationReceiver::startSubNegotiation);
    }

    public void subNegotiationData(ByteBuffer data, TelnetSession s) {
        apply(s, subNegotiationReceiver,
            (subNegotiationReceiver, session, sessionData) ->
                sessionData = subNegotiationReceiver.subNegotiationData(data, session, sessionData));
    }
}
