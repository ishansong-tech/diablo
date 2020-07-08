package com.ishansong.diablo.plugin.cat.reactor;

import com.ishansong.diablo.plugin.cat.trace.CatReactorSubscriber;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;

public class MonoCatOperator<T> extends MonoOperator<T, T> {

    private final CatConfig catConfig;

    protected MonoCatOperator(Mono<? extends T> source, CatConfig catConfig) {
        super(source);

        this.catConfig = catConfig;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        source.subscribe(new CatReactorSubscriber<>(catConfig, actual));
    }
}
