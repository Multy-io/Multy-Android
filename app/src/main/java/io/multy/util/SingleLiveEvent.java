/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public class SingleLiveEvent<T> extends MutableLiveData<T> {

    private static final String TAG = SingleLiveEvent.class.getSimpleName();
    private final AtomicBoolean pending = new AtomicBoolean(false);

    @MainThread
    public void observe(LifecycleOwner owner, final Observer<T> observer) {

        if (hasActiveObservers()) {
            Timber.tag(TAG).w("Multiple observers registered but only one will be notified of changes.");
        }

        super.observe(owner, t -> {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t);
            }
        });
    }

    @MainThread
    public void setValue(@Nullable T t) {
        pending.set(true);
        super.setValue(t);
    }

    @MainThread
    public void call() {
        setValue(null);
    }
}