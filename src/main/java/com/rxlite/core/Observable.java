package main.java.com.rxlite.core;
//package com.rxlite.core;

import main.java.com.rxlite.schedule.Scheduler;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {

    /* ------------  вложенный функциональный интерфейс ------------- */
    @FunctionalInterface
    public interface OnSubscribe<T> {
        Disposable subscribe(Observer<? super T> observer);
    }

    private final OnSubscribe<T> source;

    private Observable(OnSubscribe<T> source) {
        this.source = source;
    }

    /* ------------------------- Фабрики ----------------------------- */

    public static <T> Observable<T> create(OnSubscribe<T> source) {
        return new Observable<>(Objects.requireNonNull(source));
    }

    public static <T> Observable<T> just(T item) {
        return create(obs -> {
            obs.onNext(item);
            obs.onComplete();
            return new Disposable(null);
        });
    }

    /* ------------------------- Подписка ---------------------------- */

    public Disposable subscribe(Observer<? super T> observer) {
        return source.subscribe(observer);
    }

    /* ------------------------- Операторы --------------------------- */

    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return create(down ->
                subscribe(new Observer<>() {
                    @Override public void onNext(T t) { down.onNext(mapper.apply(t)); }
                    @Override public void onError(Throwable e) { down.onError(e); }
                    @Override public void onComplete() { down.onComplete(); }
                })
        );
    }

    public Observable<T> filter(Predicate<? super T> predicate) {
        return create(down ->
                subscribe(new Observer<>() {
                    @Override public void onNext(T t) {
                        try {
                            if (predicate.test(t)) down.onNext(t);
                        } catch (Throwable e) {
                            down.onError(e);
                        }
                    }
                    @Override public void onError(Throwable e) { down.onError(e); }
                    @Override public void onComplete() { down.onComplete(); }
                })
        );
    }

    public <R> Observable<R> flatMap(Function<? super T, Observable<? extends R>> mapper) {
        return create(down -> {
            Disposable.Composite composite = new Disposable.Composite();

            Disposable upstream = subscribe(new Observer<T>() {
                @Override
                public void onNext(T t) {
                    try {
                        Observable<? extends R> inner = mapper.apply(t);
                        Disposable innerDisp = inner.subscribe(new Observer<R>() {
                            @Override
                            public void onNext(R r) {
                                down.onNext(r);
                            }

                            @Override
                            public void onError(Throwable e) {
                                down.onError(e);
                            }

                            @Override
                            public void onComplete() {
                                // можно оставить пустым
                            }
                        });
                        composite.add(innerDisp);
                    } catch (Throwable e) {
                        down.onError(e);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    down.onError(e);
                }

                @Override
                public void onComplete() {
                    down.onComplete();
                }
            });

            composite.add(upstream);
            return composite;
        });
    }


    /* -------------------- Управление потоками ---------------------- */

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return create(down -> {
            AtomicReference<Disposable> ref = new AtomicReference<>();
            scheduler.execute(() -> ref.set(Observable.this.subscribe(down)));
            return new Disposable(() -> {
                Disposable d = ref.get();
                if (d != null) d.dispose();
            });
        });
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return create(down ->
                Observable.this.subscribe(new Observer<>() {
                    @Override public void onNext(T t) { scheduler.execute(() -> down.onNext(t)); }
                    @Override public void onError(Throwable e) { scheduler.execute(() -> down.onError(e)); }
                    @Override public void onComplete() { scheduler.execute(down::onComplete); }
                })
        );
    }
}
