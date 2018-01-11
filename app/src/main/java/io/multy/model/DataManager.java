/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model;

import android.support.annotation.NonNull;

import java.util.List;

import io.multy.Multy;
import io.multy.api.MultyApi;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.DeviceId;
import io.multy.model.entities.ExchangePrice;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.RootKey;
import io.multy.model.entities.Token;
import io.multy.model.entities.UserId;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.requests.AddWalletAddressRequest;
import io.multy.model.responses.AuthResponse;
import io.multy.model.responses.UserAssetsResponse;
import io.multy.storage.DatabaseHelper;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ihar Paliashchuk on 10.11.2017.
 * ihar.paliashchuk@gmail.com
 */

@Deprecated
public class DataManager {

    private static DatabaseHelper databaseHelper;
    private static DataManager dataManager;

    public static DataManager getInstance() {
        if (dataManager == null) {
            dataManager = new DataManager();
        }

        dataManager.refreshDatabaseHelper();
        return dataManager;
    }

    public void refreshDatabaseHelper() {
        databaseHelper = new DatabaseHelper(Multy.getContext());
    }

    public void auth(String userId, String deviceId, String password) {
        MultyApi.INSTANCE.auth(userId, deviceId, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                databaseHelper.saveToken(new Token(response.body()));
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
            }
        });
    }

    public Observable<UserAssetsResponse> getWalletAddresses(int walletId) {
        return MultyApi.INSTANCE.getWalletAddresses(walletId);
    }

    public void saveWalletAmount(WalletRealmObject walletRealmObject, double amount) {
        databaseHelper.saveAmount(walletRealmObject, amount);
    }

    public void saveRootKey(RootKey key) {
        databaseHelper.saveRootKey(key);
    }

    public RootKey getRootKey() {
        return databaseHelper.getRootKey();
    }

    public void saveToken(Token token) {
        databaseHelper.saveToken(token);
    }

    public Token getToken() {
        return databaseHelper.getToken();
    }

    public void saveUserId(UserId userId) {
        databaseHelper.saveUserId(userId);
    }

    public UserId getUserId() {
        return databaseHelper.getUserId();
    }

    public void saveSeed(ByteSeed seed) {
        databaseHelper.saveSeed(seed);
    }

    public ByteSeed getSeed() {
        return databaseHelper.getSeed();
    }

    public void saveWallets(List<WalletRealmObject> wallet) {
        databaseHelper.saveWallets(wallet);
    }

    public Observable<Object> addWalletAddress(AddWalletAddressRequest addWalletAddressRequest) {
        return MultyApi.INSTANCE.addWalletAddress(addWalletAddressRequest);
    }

    public void saveAddress(WalletRealmObject wallet, WalletAddress address) {
        databaseHelper.saveAddress(wallet, address);
    }

    public WalletRealmObject getWallet() {
        return databaseHelper.getWallet();
    }

    public Flowable<RealmResults<WalletRealmObject>> getWalletsFlowable() {
        return databaseHelper.getWallets().asFlowable();
    }

    public RealmResults<WalletRealmObject> getWallets() {
        return databaseHelper.getWallets();
    }

    public WalletRealmObject getWalletById(int walletId) {
        return databaseHelper.getWallet();
    }

    public WalletRealmObject getWallet(int id) {
        return databaseHelper.getWalletById(id);
    }

    public void setMnemonic(Mnemonic mnemonic) {
        databaseHelper.setMnemonic(mnemonic);
    }

    public Mnemonic getMnemonic() {
        return databaseHelper.getMnemonic();
    }

    public DeviceId getDeviceId() {
        return databaseHelper.getDeviceId();
    }

    public void setDeviceId(DeviceId deviceId) {
        databaseHelper.setDeviceId(deviceId);
    }

    public void saveExchangePrice(Double exchangePrice) {
        databaseHelper.saveExchangePrice(new ExchangePrice(exchangePrice));
    }

    public Double getExchangePriceDB() {
        if (databaseHelper.getExchangePrice() == null) {
            return 16000.0;
        } else {
            return databaseHelper.getExchangePrice().getExchangePrice();
        }
    }

    public void saveWallet(WalletRealmObject wallet) {
        databaseHelper.insertOrUpdate(wallet.getWalletIndex(), wallet.getName(), wallet.getAddresses(), wallet.calculateBalance(), wallet.calculatePendingBalance());
    }

    public void deleteDatabase() {
        databaseHelper.clear();
    }

    public void saveCurenciesRate(CurrenciesRate currenciesRate) {
        databaseHelper.saveCurrenciesRate(currenciesRate);
    }

    public CurrenciesRate getCurrenciesRate() {
        return databaseHelper.getCurrenciesRate();
    }

    public WalletAddress getWalletAddress(int id) {
        return databaseHelper.getWalletAddress(id);
    }

}
