/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.content.Context;

import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.DeviceId;
import io.multy.model.entities.ExchangePrice;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.RootKey;
import io.multy.model.entities.Token;
import io.multy.model.entities.UserId;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class DatabaseHelper {

    private Realm realm;

    public DatabaseHelper(Context context) {
        realm = Realm.getInstance(getRealmConfiguration(context));
    }

    private RealmConfiguration getRealmConfiguration(Context context) {
//        if (MasterKeyGenerator.generateKey(context) != null) {
//            return new RealmConfiguration.Builder()
//                    .encryptionKey(MasterKeyGenerator.generateKey(context))
//                    .build();
//        } else {
        return new RealmConfiguration.Builder().build();
//        }
    }

    public RealmResults<WalletRealmObject> getWallets(){
        return realm.where(WalletRealmObject.class).findAll();
    }

    public void saveWallet(WalletRealmObject wallet) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(wallet));
    }

    public void saveAddress(WalletRealmObject wallet, WalletAddress address) {
        realm.executeTransaction(realm -> {
            wallet.getAddresses().add(address);
            realm.insertOrUpdate(wallet);
        });
    }

    public WalletRealmObject getWallet() {
        return realm.where(WalletRealmObject.class).findFirst();
    }

    public WalletRealmObject getWalletById(int id) {
        return realm.where(WalletRealmObject.class).equalTo("walletIndex", id).findFirst();
    }

    public void saveRootKey(RootKey key) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(key));
    }

    public RootKey getRootKey() {
        return realm.where(RootKey.class).findFirst();
    }

    public void saveToken(Token token) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(token));
    }

    public Token getToken() {
        return realm.where(Token.class).findFirst();
    }

    public void saveUserId(UserId userId) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(userId));
    }

    public UserId getUserId() {
        return realm.where(UserId.class).findFirst();
    }

    public void saveSeed(ByteSeed seed) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(seed));
    }

    public ByteSeed getSeed() {
        return realm.where(ByteSeed.class).findFirst();
    }

    public void setMnemonic(Mnemonic mnemonic) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(mnemonic));
    }

    public Mnemonic getMnemonic() {
        return realm.where(Mnemonic.class).findFirst();
    }

    public void setDeviceId(DeviceId deviceId) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(deviceId));
    }

    public DeviceId getDeviceId() {
        return realm.where(DeviceId.class).findFirst();
    }

    public void saveExchangePrice(final ExchangePrice exchangePrice) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(exchangePrice));
    }

    public ExchangePrice getExchangePrice() {
        return realm.where(ExchangePrice.class).findFirst();
    }
}