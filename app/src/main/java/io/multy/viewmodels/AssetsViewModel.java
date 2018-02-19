/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;

import com.samwolfand.oneprefs.Prefs;

import java.util.List;

import io.multy.api.socket.CurrenciesRate;
import io.multy.api.socket.SocketManager;
import io.multy.api.socket.TransactionUpdateEntity;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.util.Constants;
import io.multy.util.SingleLiveEvent;

public class AssetsViewModel extends BaseViewModel implements LifecycleObserver {

    private SocketManager socketManager;

    public MutableLiveData<List<WalletRealmObject>> wallets = new MutableLiveData<>();
    public MutableLiveData<CurrenciesRate> rates = new MutableLiveData<>();
    public SingleLiveEvent<TransactionUpdateEntity> transactionUpdate = new SingleLiveEvent<>();

    public void init(Lifecycle lifecycle) {
        initRates();
        socketManager = new SocketManager();
        lifecycle.addObserver(this);
    }

    private void initRates() {
        CurrenciesRate currenciesRate = DataManager.getInstance().getCurrenciesRate();
        if (currenciesRate != null) {
            rates.setValue(currenciesRate);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onCreate() {
        if (socketManager != null) {
            socketManager = new SocketManager();
            socketManager.connect(rates, transactionUpdate);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onStop() {
        if (socketManager != null) {
            socketManager.disconnect();
        }
    }

    public List<WalletRealmObject> getWalletsFromDB() {
        return DataManager.getInstance().getWallets();
    }

    public MutableLiveData<List<WalletRealmObject>> getWallets() {
        return wallets;
    }

    public boolean isFirstStart() {
        return !Prefs.getBoolean(Constants.PREF_APP_INITIALIZED);
    }

    public int getChainId() {
        return 1;
    }
}
