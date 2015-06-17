package net.virtualinfinity.telnet.option.handlers;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

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
    public void enabledLocally() {
        apply(optionReceiver, OptionReceiver::enabledLocally);
    }

    public void disabledLocally() {
        apply(optionReceiver, OptionReceiver::disabledLocally);
    }
    public void enabledRemotely() {
        apply(optionReceiver, OptionReceiver::enabledRemotely);
    }

    public void disabledRemotely() {
        apply(optionReceiver, OptionReceiver::disabledRemotely);
    }

    private <R> void apply(R receiver, BiFunction<R, T, T> operation) {
        if (receiver == null) {
            return;
        }
        sessionData = operation.apply(receiver, sessionData);
    }

    public void endSubNegotiation() {
        apply(subNegotiationReceiver, SubNegotiationReceiver::endSubNegotiation);
    }

    public void startSubNegotiation() {
        apply(subNegotiationReceiver, SubNegotiationReceiver::startSubNegotiation);
    }

    public void subNegotiationData(ByteBuffer data) {
        apply(subNegotiationReceiver,
            (subNegotiationReceiver, sessionData) ->
                sessionData = subNegotiationReceiver.subNegotiationData(data, sessionData));
    }
}
