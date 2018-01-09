/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;

import com.samwolfand.oneprefs.Prefs;

import java.util.List;

import io.multy.api.MultyApi;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.activities.AssetActivity;
import io.multy.util.Constants;
import io.multy.util.FirstLaunchHelper;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class WalletViewModel extends BaseViewModel {

    private MutableLiveData<WalletRealmObject> wallet = new MutableLiveData<>();
    private MutableLiveData<Double> exchangePrice = new MutableLiveData<>();
    public MutableLiveData<String> chainCurrency = new MutableLiveData<>();
    public MutableLiveData<String> fiatCurrency = new MutableLiveData<>();
    private MutableLiveData<List<WalletAddress>> addresses = new MutableLiveData<>();

    public WalletViewModel() {
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
        DataManager.getInstance().getWalletAddresses(walletId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {
                    Timber.e("addresses %s", response);
                }, Throwable::printStackTrace);
    }

    public Double getApiExchangePrice() {
        DataManager dataManager = DataManager.getInstance();
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
        WalletRealmObject wallet = DataManager.getInstance().getWallet(index);
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
            DataManager dataManager = DataManager.getInstance();

            List<WalletRealmObject> wallets = dataManager.getWallets();

            final int walletIndex = wallets == null ? 0 : wallets.size();
            final int currency = NativeDataHelper.Currency.BTC.getValue(); //TODO implement choosing crypto currency using enum NativeDataHelper.CURRENCY

            if (!Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
                FirstLaunchHelper.setCredentials("");
            }

            String creationAddress = NativeDataHelper.makeAccountAddress(dataManager.getSeed().getSeed(), walletIndex, 0, currency);

            walletRealmObject = new WalletRealmObject();
            walletRealmObject.setName(walletName);

            RealmList<WalletAddress> addresses = new RealmList<>();
            addresses.add(new WalletAddress(0, creationAddress));

            walletRealmObject.setAddresses(addresses);
            walletRealmObject.setCurrency(0);
            walletRealmObject.setAddressIndex(0);
            walletRealmObject.setCreationAddress(creationAddress);
            walletRealmObject.setWalletIndex(walletIndex);

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
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    DataManager.getInstance().saveWallet(walletRealmObject);
                    Prefs.putBoolean(Constants.PREF_APP_INITIALIZED, true);

                    Intent intent = new Intent(activity, AssetActivity.class);
                    if (walletRealmObject != null) {
                        intent.putExtra(Constants.EXTRA_WALLET_ID, walletRealmObject.getWalletIndex());
                    }

                    activity.startActivity(intent);
                    activity.finish();
                } else {
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
