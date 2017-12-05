/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.multy.model.entities.Wallet;
import io.realm.Realm;
import io.realm.RealmConfiguration;

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

    public void saveWallets() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
//                realm.insertOrUpdate();
            }
        });
    }

    public List<Wallet> getWallets() {
//        return realm.where(Wallet.class).findAll();
        return new ArrayList<>();
    }

    public void saveRootKey() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
            }
        });
    }

    public void saveToken() {

    }

    public void getToken() {

    }
}