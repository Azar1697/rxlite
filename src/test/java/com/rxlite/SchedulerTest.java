package test.java.com.rxlite;

import main.java.com.rxlite.core.Disposable;
import main.java.com.rxlite.core.Observable;
import main.java.com.rxlite.core.Observer;
import main.java.com.rxlite.schedule.IOThreadScheduler;
import main.java.com.rxlite.schedule.SingleThreadScheduler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

    @Test
    void subscribeOnObserveOn() throws InterruptedException {
        // сюда запишем поток, в котором выполнялся subscribeOn-блок
        final String[] subscribeThread = new String[1];
        // сюда запишем поток observeOn
        final StringBuilder observeThread = new StringBuilder();

        // создаём «холодный» Observable, чтобы точно попасть в subscribeOn
        Observable<Integer> source = Observable.create(down -> {
            subscribeThread[0] = Thread.currentThread().getName(); // фиксируем поток subscribe
            down.onNext(1);
            down.onComplete();
            return new Disposable(null);
        });

        source
                .subscribeOn(new IOThreadScheduler())          // I/O pool
                .observeOn(new SingleThreadScheduler())        // Single thread
                .subscribe(new Observer<>() {
                    @Override public void onNext(Integer item) {
                        observeThread.append(Thread.currentThread().getName());
                    }
                    @Override public void onError(Throwable t) { }
                    @Override public void onComplete() { }
                });

        Thread.sleep(500); // ждём, пока всё отработает

        assertNotNull(subscribeThread[0], "subscribeOn thread was not captured");
        assertFalse(observeThread.isEmpty(), "observeOn thread was not captured");
        assertNotEquals(
                subscribeThread[0],
                observeThread.toString(),
                "Expected different threads for subscribeOn and observeOn"
        );
    }
}
