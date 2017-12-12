/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import io.multy.util.SingleLiveEvent;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public class BaseViewModel extends ViewModel {

    public SingleLiveEvent<String> errorMessage = new SingleLiveEvent<>();
    public SingleLiveEvent<String> criticalMessage = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> isLoading = new SingleLiveEvent<>();

    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    public void destroy() {
        dispose();
    }

    final void addDisposable(@NonNull Disposable disposable, @NonNull Disposable... disposables) {
        this.disposables.add(disposable);

        for (Disposable d : disposables) {
            this.disposables.add(d);
        }
    }

    @CallSuper
    private void dispose() {
        disposables.clear();
    }
}
