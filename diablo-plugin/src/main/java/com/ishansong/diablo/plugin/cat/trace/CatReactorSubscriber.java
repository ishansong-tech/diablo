package com.ishansong.diablo.plugin.cat.trace;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.AbstractMessage;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.ishansong.diablo.plugin.cat.reactor.CatConfig;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;

@Slf4j
public class CatReactorSubscriber<T> extends BaseSubscriber<T> {

    private CatConfig catConfig;

    private final CoreSubscriber<? super T> actual;

    public CatReactorSubscriber(CatConfig catConfig, CoreSubscriber<? super T> actual) {
        this.catConfig = catConfig;
        this.actual = actual;
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {

        actual.onSubscribe(this);
    }

    @Override
    protected void hookOnNext(T value) {
        if (isDisposed()) {

            log.warn("CatReactorSubscriber cancelledSubscription cat={}", catConfig);

            return;
        }

        actual.onNext(value);

    }

    @Override
    protected void hookOnComplete() {

        DefaultTransaction transaction = (DefaultTransaction) Cat.newTransaction(catConfig.getTransactionType(), catConfig.getApiName());
        transaction.setDurationStart(catConfig.getDurationStart());

        createConsumerCross(transaction);

        transaction.setStatus(Transaction.SUCCESS);
        transaction.complete();

        actual.onComplete();
    }

    @Override
    protected void hookOnError(Throwable throwable) {

        DefaultTransaction transaction = (DefaultTransaction) Cat.newTransaction(catConfig.getTransactionType(), catConfig.getApiName());
        transaction.setDurationStart(catConfig.getDurationStart());

        createConsumerCross(transaction, throwable);

        transaction.setStatus(throwable);
        transaction.complete();

        actual.onError(throwable);
    }

    @Override
    protected void hookOnCancel() {

    }

    @Override
    protected void hookFinally(SignalType type) {

    }

    private void createConsumerCross(Transaction transaction) {

        AbstractMessage gatewayCallEvent = (AbstractMessage) Cat.newEvent("GatewayCall", catConfig.getRouteHost());
        gatewayCallEvent.setTimestamp(catConfig.getEventStart());

        gatewayCallEvent.setStatus(Event.SUCCESS);
        gatewayCallEvent.setCompleted(true);

        transaction.addChild(gatewayCallEvent);
    }

    private void createConsumerCross(Transaction transaction, Throwable throwable) {

        AbstractMessage gatewayCallEvent = (AbstractMessage) Cat.newEvent("GatewayCall", catConfig.getRouteHost());
        gatewayCallEvent.setTimestamp(catConfig.getEventStart());

        gatewayCallEvent.setStatus(throwable);
        gatewayCallEvent.setCompleted(true);

        transaction.addChild(gatewayCallEvent);
    }

    private void entrySubscribed() {

        Transaction transaction = Cat.newTransaction(catConfig.getTransactionType(), catConfig.getApiName());
        try {

            createConsumerCross(transaction);
            // this.transaction = ThreadLocal.withInitial(() -> transaction);
            actual.onSubscribe(this);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.setStatus(e.getClass().getCanonicalName());
            }

            Cat.logError(e);
            throw e;
        }
    }

}
