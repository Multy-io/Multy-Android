/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import io.multy.util.SingleLiveEvent;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public class BaseViewModel extends ViewModel {

    public SingleLiveEvent<String> errorMessage = new SingleLiveEvent<>();
    public SingleLiveEvent<String> criticalMessage = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> isLoading = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> isConnectionAvailable = new SingleLiveEvent<>();

    {
        isConnectionAvailable.setValue(true);
    }

    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    final void addDisposable(@NonNull Disposable disposable, @NonNull Disposable... disposables) {
        this.disposables.add(disposable);

        for (Disposable d : disposables) {
            this.disposables.add(d);
        }
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
