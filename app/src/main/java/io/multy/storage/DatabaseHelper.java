/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.RootKey;
import io.multy.model.entities.Token;
import io.multy.model.entities.UserId;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DatabaseHelper {

    private Realm realm;

    public DatabaseHelper(Context context) {
        realm = Realm.getInstance(getRealmConfiguration(context));
    }

    private RealmConfiguration getRealmConfiguration(Context context){
//        if (MasterKeyGenerator.generateKey(context) != null) {
//            return new RealmConfiguration.Builder()
//                    .encryptionKey(MasterKeyGenerator.generateKey(context))
//                    .build();
//        } else {
            return new RealmConfiguration.Builder().build();
//        }
    }

    public void saveWallets(){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
//                realm.insertOrUpdate();
            }
        });
    }

    public List<Wallet> getWallets(){
//        return realm.where(Wallet.class).findAll();
        return new ArrayList<>();
    }

    public void saveWallet(WalletRealmObject wallet){
        realm.executeTransaction(realm -> realm.insertOrUpdate(wallet));
    }

    public WalletRealmObject getWallet(){
        return realm.where(WalletRealmObject.class).findFirst();
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


}