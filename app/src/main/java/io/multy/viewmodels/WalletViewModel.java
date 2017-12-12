/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import java.util.List;

import io.multy.Multy;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.responses.UserAssetsResponse;
import io.multy.model.responses.WalletInfo;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class WalletViewModel extends BaseViewModel {

    private DataManager dataManager;
    private MutableLiveData<WalletRealmObject> wallet = new MutableLiveData<>();
    private MutableLiveData<Double> exchangePrice = new MutableLiveData<>();
    public MutableLiveData<String> chainCurrency = new MutableLiveData<>();
    public MutableLiveData<String> fiatCurrency = new MutableLiveData<>();
    private MutableLiveData<List<WalletAddress>> addresses = new MutableLiveData<>();

    public WalletViewModel() {
    }

    public void setContext(Context context) {
        dataManager = new DataManager(context);
    }

//    public void getUserAssets() {
//        Disposable disposable = dataManager.getUserAssets()
//                .map(UserAssetsResponse::getWalletInfo)
//                .flatMapIterable(walletsInfo -> walletsInfo)
//                .map(WalletInfo::getAddress)
//                .flatMapIterable(addresses -> addresses)
//                .toList()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe(response -> addresses.setValue(response), Throwable::printStackTrace);
//
//        addDisposable(disposable);
//    }

    public void getWalletAddresses(int walletId) {
        dataManager.getWalletAddresses(walletId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {
                    Timber.e("addresses %s", response);
                }, Throwable::printStackTrace);
    }

    public void getApiExchangePrice() {
        dataManager.getExchangePrice(CurrencyCode.BTC.name(), CurrencyCode.USD.name())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(response -> exchangePrice.setValue(response.getUSD()), Throwable::printStackTrace);
    }

    public void getWalletLive(int walletId) {

    }

    public MutableLiveData<Double> getExchangePrice() {
        return exchangePrice;
    }

    public void addWallet() {

    }

    public MutableLiveData<List<WalletAddress>> getAddresses() {
        return addresses;
    }

    public WalletRealmObject getWallet(int index) {
        dataManager = new DataManager(Multy.getContext());
        WalletRealmObject wallet = dataManager.getWallet(index);
        this.wallet.setValue(wallet);
        return wallet;
    }

    public MutableLiveData<WalletRealmObject> getWalletLive() {
        return wallet;
    }


}
