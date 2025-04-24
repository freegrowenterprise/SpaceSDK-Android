package com.growspace.sdk;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseObservable<LISTENER_CLASS> {
    private final Set<LISTENER_CLASS> mListeners = Collections.newSetFromMap(new ConcurrentHashMap<>(1));

    public final void registerListener(LISTENER_CLASS listener_class) {
        this.mListeners.add(listener_class);
    }

    public final void unregisterListener(LISTENER_CLASS listener_class) {
        this.mListeners.remove(listener_class);
    }

    protected final Set<LISTENER_CLASS> getListeners() {
        return Collections.unmodifiableSet(this.mListeners);
    }
}
