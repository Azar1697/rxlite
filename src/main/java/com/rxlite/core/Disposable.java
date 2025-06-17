package main.java.com.rxlite.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Disposable {
    private final AtomicBoolean disposed = new AtomicBoolean(false);
    private final Runnable onDispose;          // может быть null

    public Disposable(Runnable onDispose) {
        this.onDispose = onDispose;
    }

    public void dispose() {
        if (disposed.compareAndSet(false, true) && onDispose != null) {
            onDispose.run();
        }
    }

    public boolean isDisposed() {
        return disposed.get();
    }

    /* ----------- Composite — группа Disposable-ов ----------------- */
    public static final class Composite extends Disposable {
        private final List<Disposable> list = new CopyOnWriteArrayList<>();

        public Composite() {
            super(null);
        }

        public void add(Disposable d) {
            if (!isDisposed()) list.add(d);
        }

        @Override public void dispose() {
            super.dispose();
            list.forEach(Disposable::dispose);
        }
    }
}
