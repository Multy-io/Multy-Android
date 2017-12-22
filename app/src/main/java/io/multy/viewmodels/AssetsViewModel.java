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
import android.content.Context;

import com.samwolfand.oneprefs.Prefs;

import java.util.ArrayList;
import java.util.List;

import io.multy.Multy;
import io.multy.api.socket.CurrenciesRate;
import io.multy.api.socket.GraphPoint;
import io.multy.api.socket.SocketManager;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.util.Constants;

public class AssetsViewModel extends BaseViewModel implements LifecycleObserver {

    private DataManager dataManager;
    private SocketManager socketManager;
    public MutableLiveData<List<WalletRealmObject>> wallets = new MutableLiveData<>();
    public MutableLiveData<CurrenciesRate> rates = new MutableLiveData<>();
    public MutableLiveData<ArrayList<GraphPoint>> graphPoints = new MutableLiveData<>();

    public void init(Lifecycle lifecycle) {
        dataManager = new DataManager(Multy.getContext());
        initRates();
        socketManager = new SocketManager();
        lifecycle.addObserver(this);
    }

    private void initRates() {
        CurrenciesRate currenciesRate = dataManager.getCurrenciesRate();
        if (currenciesRate != null) {
            rates.setValue(currenciesRate);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onCreate() {
        if (socketManager != null) {
            socketManager = new SocketManager();
            socketManager.connect(rates, graphPoints);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onStop() {
        if (socketManager != null) {
            socketManager.disconnect();
        }
    }

    public void setContext(Context context) {
        dataManager = new DataManager(context);
    }


    public List<WalletRealmObject> getWalletsFromDB() {
        return dataManager.getWallets();
    }

    public MutableLiveData<List<WalletRealmObject>> getWallets() {
        return wallets;
    }

    public boolean isFirstStart() {
        return Prefs.getBoolean(Constants.PREF_IS_FIRST_START, true);
    }
}
