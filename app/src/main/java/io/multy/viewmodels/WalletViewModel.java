/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import java.util.List;

import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.responses.UserAssetsResponse;
import io.multy.model.responses.WalletInfo;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class WalletViewModel extends BaseViewModel {

    private DataManager dataManager;
    public MutableLiveData<WalletRealmObject> walletLive = new MutableLiveData<>();
    public WalletRealmObject wallet = new WalletRealmObject();
    public MutableLiveData<String> chainCurrency = new MutableLiveData<>();
    public MutableLiveData<String> fiatCurrency = new MutableLiveData<>();
    private MutableLiveData<List<WalletAddress>> addresses = new MutableLiveData<>();

    public WalletViewModel() {
    }

    public void setContext(Context context) {
        dataManager = new DataManager(context);
    }

    public void getUserAssets() {
        Disposable disposable = dataManager.getUserAssets()
                .map(UserAssetsResponse::getWalletInfo)
                .flatMapIterable(walletsInfo -> walletsInfo)
                .map(WalletInfo::getAddress)
                .flatMapIterable(addresses -> addresses)
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(response -> addresses.setValue(response), Throwable::printStackTrace);

        addDisposable(disposable);
    }

    public void getWalletAddresses(int walletId) {
        dataManager.getWalletAddresses(walletId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {
                    Timber.e("addresses %s", response);
                }, Throwable::printStackTrace);
    }

    public void getWalletLive(int walletId) {

    }

    public void getExchangePrice() {

    }

    public void addWallet(){

    }

    public MutableLiveData<List<WalletAddress>> getAddresses() {
        return addresses;
    }

    public void setWalletLive(WalletRealmObject wallet) {
        Timber.e("setWalletLive %s", wallet.toString());
        this.walletLive.setValue(wallet);
    }

    public void setWallet(WalletRealmObject wallet){
        Timber.e("setWallet %s", wallet.toString());
        this.wallet = wallet;
    }

    public WalletRealmObject getWallet() {
        return wallet;
    }

    public MutableLiveData<WalletRealmObject> getWalletLive() {
        return walletLive;
    }


}
