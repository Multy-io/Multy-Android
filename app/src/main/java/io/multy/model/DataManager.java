/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import io.multy.api.MultyApi;
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
import io.multy.model.responses.ExchangePriceResponse;
import io.multy.model.responses.UserAssetsResponse;
import io.multy.storage.DatabaseHelper;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by Ihar Paliashchuk on 10.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class DataManager {

    private DatabaseHelper database;

    public DataManager(Context context) {
        this.database = new DatabaseHelper(context);
    }

    public void auth(String userId, String deviceId, String password) {
        MultyApi.INSTANCE.auth(userId, deviceId, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                database.saveToken(new Token(response.body()));
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
            }
        });
        ;
    }

    public Observable<ExchangePriceResponse> getExchangePrice(String originalCurrency, String currency) {
        return MultyApi.INSTANCE.getExchangePrice(originalCurrency, currency)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnNext(exchangePriceResponse -> database.saveExchangePrice(new ExchangePrice(exchangePriceResponse.getUSD())));
    }

    public Observable<UserAssetsResponse> getUserAssets(){
        return MultyApi.INSTANCE.getUserAssets();
    }

    public Observable<UserAssetsResponse> getWalletAddresses(int walletId){
        return MultyApi.INSTANCE.getWalletAddresses(walletId);
    }

    public Observable<List<WalletRealmObject>> restore(){
        return MultyApi.INSTANCE.restore()
                .doOnNext(walletRealmObjects -> {
                    Timber.i("wallets %s", walletRealmObjects.toString());
                    database.saveWallets(walletRealmObjects);
                });
    }

    public void saveWalletAmount(WalletRealmObject walletRealmObject, double amount){
        database.saveAmount(walletRealmObject, amount);
    }

    public void saveRootKey(RootKey key) {
        database.saveRootKey(key);
    }

    public RootKey getRootKey() {
        return database.getRootKey();
    }

    public void saveToken(Token token) {
        database.saveToken(token);
    }

    public Token getToken() {
        return database.getToken();
    }

    public void saveUserId(UserId userId) {
        database.saveUserId(userId);
    }

    public UserId getUserId() {
        return database.getUserId();
    }

    public void saveSeed(ByteSeed seed) {
        database.saveSeed(seed);
    }

    public ByteSeed getSeed() {
        return database.getSeed();
    }

    public void saveWallet(WalletRealmObject wallet) {
        database.saveWallet(wallet);
    }

    public Observable<Object> addWalletAddress(AddWalletAddressRequest addWalletAddressRequest){
        return MultyApi.INSTANCE.addWalletAddress(addWalletAddressRequest);
    }

    public void saveAddress(WalletRealmObject wallet, WalletAddress address) {
        database.saveAddress(wallet, address);
    }

    public WalletRealmObject getWallet() {
        return database.getWallet();
    }

    public Flowable<RealmResults<WalletRealmObject>> getWalletsFlowable() {
        return database.getWallets().asFlowable();
    }

    public RealmResults<WalletRealmObject> getWallets() {
        return database.getWallets();
    }

    public WalletRealmObject getWalletById(int walletId) {
        return database.getWallet();
    }

    public WalletRealmObject getWallet(int id) {
        return database.getWalletById(id);
    }

    public void setMnemonic(Mnemonic mnemonic) {
        database.setMnemonic(mnemonic);
    }

    public Mnemonic getMnemonic() {
        return database.getMnemonic();
    }

    public DeviceId getDeviceId() {
        return database.getDeviceId();
    }

    public void setDeviceId(DeviceId deviceId) {
        database.setDeviceId(deviceId);
    }

    public void saveExchangePrice(Double exchangePrice){
        database.saveExchangePrice(new ExchangePrice(exchangePrice));
    }

    public Double getExchangePriceDB() {
        if (database.getExchangePrice() == null) {
            return 16000.0;
        } else {
            return database.getExchangePrice().getExchangePrice();
        }
    }

    public void updateWallet(int index, RealmList<WalletAddress> addresses, double balance, double pendingBalance) {
        database.updateWallet(index, addresses, balance, pendingBalance);
    }

    public void deleteDatabase(){
        database.clear();
    }
}
