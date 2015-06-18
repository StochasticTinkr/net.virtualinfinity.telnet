package net.virtualinfinity.telnet.option.handlers;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public final class OptionSessionHandlerImpl<T> implements OptionSessionHandler<T> {
    private final SubNegotiationReceiver<T> subNegotiationReceiver;
    private final OptionReceiver<T> optionReceiver;
    private T sessionData;

    public OptionSessionHandlerImpl(SubNegotiationReceiver<T> handler, OptionReceiver<T> optionReceiver) {
        this.subNegotiationReceiver = handler;
        this.optionReceiver = optionReceiver;
    }

    public static  <T, R extends SubNegotiationReceiver<T>&OptionReceiver<T>> OptionSessionHandler<T> of(R receiver) {
        return new OptionSessionHandlerImpl<>(receiver, receiver);
    }

    public static <T> OptionSessionHandler<T> of(OptionReceiver<T> receiver) {
        return new OptionSessionHandlerImpl<>(null, receiver);
    }
    @Override
    public void enabledLocally() {
        apply(optionReceiver, OptionReceiver::enabledLocally);
    }

    @Override
    public void disabledLocally() {
        apply(optionReceiver, OptionReceiver::disabledLocally);
    }
    @Override
    public void enabledRemotely() {
        apply(optionReceiver, OptionReceiver::enabledRemotely);
    }

    @Override
    public void disabledRemotely() {
        apply(optionReceiver, OptionReceiver::disabledRemotely);
    }

    private <R> void apply(R receiver, BiFunction<R, T, T> operation) {
        if (receiver == null) {
            return;
        }
        sessionData = operation.apply(receiver, sessionData);
    }

    @Override
    public void endSubNegotiation() {
        apply(subNegotiationReceiver, SubNegotiationReceiver::endSubNegotiation);
    }

    @Override
    public void startSubNegotiation() {
        apply(subNegotiationReceiver, SubNegotiationReceiver::startSubNegotiation);
    }

    @Override
    public void subNegotiationData(ByteBuffer data) {
        apply(subNegotiationReceiver,
            (subNegotiationReceiver, sessionData) ->
                sessionData = subNegotiationReceiver.subNegotiationData(data, sessionData));
    }
}
