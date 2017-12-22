/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import io.multy.Multy;
import io.multy.api.MultyApi;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.responses.UserAssetsResponse;
import io.multy.model.responses.WalletInfo;
import io.multy.ui.activities.AssetActivity;
import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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

    public Double getApiExchangePrice() {
        dataManager.getExchangePrice(CurrencyCode.BTC.name(), CurrencyCode.USD.name())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(response -> exchangePrice.setValue(response.getUSD()), Throwable::printStackTrace);

        if (dataManager.getExchangePriceDB() != null) {
            exchangePrice.setValue(dataManager.getExchangePriceDB());
            return dataManager.getExchangePriceDB();
        } else {
            exchangePrice.setValue(16000.0);
            return 16000.0;
        }
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

    public void createWallet(Activity activity, String walletName) {
        isLoading.setValue(true);
        WalletRealmObject walletRealmObject = null;
        try {
            List<WalletRealmObject> wallets = dataManager.getWallets();
            final int index = wallets.size();
            final int currency = NativeDataHelper.Currency.BTC.getValue(); //TODO implement choosing crypto currency using enum NativeDataHelper.CURRENCY
            String creationAddress = NativeDataHelper.makeAccountAddress(dataManager.getSeed().getSeed(), index, currency);
            walletRealmObject = new WalletRealmObject();
            walletRealmObject.setName(walletName);
            RealmList<WalletAddress> addresses = new RealmList<>();
            addresses.add(new WalletAddress(0, creationAddress));
            walletRealmObject.setAddresses(addresses);
//            if (textViewChainCurrency.getText().toString().equals(Constants.BTC)) {
            walletRealmObject.setCurrency(0);
//            } else {
//                walletRealmObject.setCurrency(1);
//            }
            walletRealmObject.setAddressIndex(0);
            walletRealmObject.setCreationAddress(creationAddress);
            walletRealmObject.setWalletIndex(index);
            saveWallet(activity, walletRealmObject);
        } catch (JniException e) {
            e.printStackTrace();
            isLoading.setValue(false);
            errorMessage.setValue(e.getLocalizedMessage());
            errorMessage.call();
        }
    }

    private void saveWallet(Activity activity, WalletRealmObject walletRealmObject) {
        Call<ResponseBody> responseBodyCall = MultyApi.INSTANCE.addWallet(activity, walletRealmObject);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    isLoading.setValue(false);
                    dataManager.saveWallet(walletRealmObject);

                    Intent intent = new Intent(activity, AssetActivity.class);
                    if (walletRealmObject != null) {
                        intent.putExtra(Constants.EXTRA_WALLET_ID, walletRealmObject.getWalletIndex());
                    }

                    activity.startActivity(intent);
                    activity.finish();
                } else {
                    isLoading.setValue(false);
                    errorMessage.call();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getLocalizedMessage());
                errorMessage.call();
                t.printStackTrace();
            }
        });
    }


}
