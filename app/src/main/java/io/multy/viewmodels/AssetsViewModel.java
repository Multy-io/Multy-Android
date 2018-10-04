/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;

import com.samwolfand.oneprefs.Prefs;

import java.net.URISyntaxException;
import java.util.List;

import io.multy.api.socket.CurrenciesRate;
import io.multy.api.socket.SocketManager;
import io.multy.api.socket.TransactionUpdateEntity;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.util.Constants;
import io.multy.util.SingleLiveEvent;

public class AssetsViewModel extends BaseViewModel implements LifecycleObserver {

    private SocketManager socketManager;

    public MutableLiveData<List<Wallet>> wallets = new MutableLiveData<>();
    public MutableLiveData<CurrenciesRate> rates = new MutableLiveData<>();
    public SingleLiveEvent<TransactionUpdateEntity> transactionUpdate = new SingleLiveEvent<>();

    public void init(Lifecycle lifecycle) {
        initRates();
        lifecycle.addObserver(this);
    }

    private void initRates() {
        CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        if (currenciesRate != null) {
            rates.setValue(currenciesRate);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onCreate() {
        try {
            if (socketManager == null) {
                socketManager = new SocketManager();
            }
            socketManager.listenRatesAndTransactions(rates, transactionUpdate);
            socketManager.listenEvent(SocketManager.getEventReceive(RealmManager.getSettingsDao().getUserId().getUserId()), args -> {
                transactionUpdate.postValue(new TransactionUpdateEntity());
            });
            socketManager.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onStop() {
        if (socketManager != null) {
            socketManager.disconnect();
        }
    }

    public List<Wallet> getWalletsFromDB() {
        return RealmManager.getAssetsDao().getWallets();
    }

    public MutableLiveData<List<Wallet>> getWallets() {
        return wallets;
    }

    public boolean isFirstStart() {
        return !Prefs.getBoolean(Constants.PREF_APP_INITIALIZED);
    }

    public int getChainId() {
        return 1;
    }
}
