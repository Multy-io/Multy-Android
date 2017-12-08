/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import java.util.List;

import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AssetsViewModel extends BaseViewModel {

    private DataManager dataManager;
    public MutableLiveData<List<WalletRealmObject>> wallets = new MutableLiveData<>();

    public AssetsViewModel() {
    }

    public void setContext(Context context) {
        dataManager = new DataManager(context);
    }

    public void getWalletsFlowable() {
        dataManager.getWalletsFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(walletList -> wallets.setValue(walletList), Throwable::printStackTrace);
    }

    public List<WalletRealmObject> getWalletsFromDB(){
        return dataManager.getWallets();
    }

    public MutableLiveData<List<WalletRealmObject>> getWallets(){
        return wallets;
    }
}
