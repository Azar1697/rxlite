package test.java.com.rxlite;

//package com.rxlite;

import main.java.com.rxlite.core.Disposable;
import main.java.com.rxlite.core.Observable;
import main.java.com.rxlite.core.Observer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ObservableTest {

    @Test
    void mapFilterFlatMap() {
        List<Integer> out = new ArrayList<>();

        Observable.just(1)
                .map(i -> i + 1)          // 2
                .filter(i -> i % 2 == 0)  // 2
                .flatMap(i -> Observable.just(i * 10)) // 20
                .subscribe(new Observer<>() {
                    @Override public void onNext(Integer item) { out.add(item); }
                    @Override public void onError(Throwable t) { fail(t); }
                    @Override public void onComplete() {}
                });

        assertEquals(List.of(20), out);
    }

    @Test
    void errorPropagation() {
        RuntimeException boom = new RuntimeException("boom");
        AtomicBoolean gotError = new AtomicBoolean(false);

        Observable.<Integer>create(obs -> {
            obs.onError(boom);
            return new Disposable(null);
        }).subscribe(new Observer<>() {
            @Override public void onNext(Integer item) {}
            @Override public void onError(Throwable t) {
                assertSame(boom, t);
                gotError.set(true);
            }
            @Override public void onComplete() {}
        });

        assertTrue(gotError.get());
    }
}
