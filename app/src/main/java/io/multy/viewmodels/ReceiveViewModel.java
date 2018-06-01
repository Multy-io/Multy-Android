/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import io.multy.model.entities.wallet.Wallet;

public class ReceiveViewModel extends ViewModel {

    private MutableLiveData<Wallet> wallet = new MutableLiveData<>();
    private MutableLiveData<Long> requestSum = new MutableLiveData<>();

    public Wallet getWallet() {
        return wallet.getValue();
    }

    public Long getRequestSum() {
        return requestSum.getValue();
    }

    public void setWallet(Wallet wallet) {
        this.wallet.setValue(wallet);
    }

    public void setRequestSum(long requestSum) {
        this.requestSum.setValue(requestSum);
    }
}
