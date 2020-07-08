package com.ishansong.diablo.plugin.cat.reactor;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class CatReactorTransformer<T> implements Function<Publisher<T>, Publisher<T>> {

    private final CatConfig catConfig;

    public CatReactorTransformer(CatConfig catConfig) {

        this.catConfig = catConfig;
    }

    @Override
    public Publisher<T> apply(Publisher<T> publisher) {

        if (publisher instanceof Mono) {
            return new MonoCatOperator<>((Mono<T>) publisher, catConfig);
        }

        if (publisher instanceof Flux) {
            return new FluxCatOperator<>((Flux<T>) publisher, catConfig);
        }

        throw new IllegalStateException("Publisher type is not supported" + publisher.getClass().getCanonicalName());
    }
}
