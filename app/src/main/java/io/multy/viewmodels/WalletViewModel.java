/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.samwolfand.oneprefs.Prefs;

import java.util.List;

import io.multy.Multy;
import io.multy.api.MultyApi;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.storage.RealmManager;
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
    private MutableLiveData<Boolean> isRemoved = new MutableLiveData<>();

    public WalletViewModel() {
    }

    public void getWalletAddresses(int walletId) {
        DataManager.getInstance().getWalletAddresses(walletId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {
                    Timber.e("addresses %s", response);
                }, Throwable::printStackTrace);
    }

    public Double getApiExchangePrice() {
        return RealmManager.getSettingsDao().getExchangePrice().getExchangePrice();
    }

    public MutableLiveData<Double> getExchangePrice() {
        return exchangePrice;
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

    public WalletRealmObject createWallet(String walletName) {
        isLoading.setValue(true);
        WalletRealmObject walletRealmObject = null;
        try {
            if (!Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
                Multy.makeInitialized();
                FirstLaunchHelper.setCredentials("");
            }
            DataManager dataManager = DataManager.getInstance();

            final int topIndex = Prefs.getInt(Constants.PREF_WALLET_TOP_INDEX);
            final int walletIndex = topIndex == Constants.ZERO ? topIndex : topIndex + Constants.ONE;
            final int currency = NativeDataHelper.Currency.BTC.getValue(); //TODO implement choosing crypto currency using enum NativeDataHelper.CURRENCY

            if (!Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
                FirstLaunchHelper.setCredentials("");
            }

            String creationAddress = NativeDataHelper.makeAccountAddress(dataManager.getSeed().getSeed(), walletIndex, Constants.ZERO, currency);

            walletRealmObject = new WalletRealmObject();
            walletRealmObject.setName(walletName);

            RealmList<WalletAddress> addresses = new RealmList<>();
            addresses.add(new WalletAddress(Constants.ZERO, creationAddress));

            walletRealmObject.setAddresses(addresses);
            walletRealmObject.setCurrency(Constants.ZERO);
            walletRealmObject.setAddressIndex(Constants.ZERO);
            walletRealmObject.setCreationAddress(creationAddress);
            walletRealmObject.setWalletIndex(walletIndex);
        } catch (JniException e) {
            e.printStackTrace();
            isLoading.setValue(false);
            errorMessage.setValue(e.getLocalizedMessage());
            errorMessage.call();
        }
        return walletRealmObject;
    }

    public MutableLiveData<Boolean> removeWallet() {
        isLoading.setValue(true);
        MultyApi.INSTANCE.removeWallet(wallet.getValue().getWalletIndex()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                RealmManager.getAssetsDao().removeWallet(wallet.getValue().getWalletIndex());
                isLoading.setValue(false);
                isRemoved.setValue(true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                throwable.printStackTrace();
                isLoading.setValue(false);
                errorMessage.setValue(throwable.getMessage());
            }
        });
        return isRemoved;
    }

}
