package com.ishansong.diablo.plugin.cat.reactor;

import com.ishansong.diablo.plugin.cat.trace.CatReactorSubscriber;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;

public class FluxCatOperator<T> extends FluxOperator<T, T> {

    private final CatConfig catConfig;

    protected FluxCatOperator(Flux<? extends T> source, CatConfig catConfig) {
        super(source);

        this.catConfig = catConfig;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        source.subscribe(new CatReactorSubscriber<>(catConfig, actual));
    }
}
