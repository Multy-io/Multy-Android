/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.content.Context;

import java.util.List;

import io.multy.Multy;
import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.DeviceId;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.UserId;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

@Deprecated
public class DatabaseHelper {

    private Realm realm;

    public DatabaseHelper(Context context) {
        try {
            realm = Realm.getInstance(Multy.getRealmConfiguration());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public RealmResults<WalletRealmObject> getWallets() {
        return realm.where(WalletRealmObject.class).findAll();
    }

    public void saveWallets(List<WalletRealmObject> wallets) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(wallets));
    }


    public WalletRealmObject getWallet() {
        return realm.where(WalletRealmObject.class).findFirst();
    }

    public WalletRealmObject getWalletById(int id) {
        return realm.where(WalletRealmObject.class).equalTo("walletIndex", id).findFirst();
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

    public void insertOrUpdate(int index, String name, RealmList<WalletAddress> addresses, double balance, double pendingBalance) {
        realm.executeTransaction(realm -> {
            WalletRealmObject savedWallet = getWalletById(index);
            if (savedWallet == null) {
                savedWallet = new WalletRealmObject();
                savedWallet.setWalletIndex(index);
            }
            savedWallet.setName(name);
            savedWallet.setAddresses(new RealmList<>());
            for (WalletAddress walletAddress : addresses) {
                savedWallet.getAddresses().add(realm.copyToRealm(walletAddress));
            }
            savedWallet.setBalance(balance);
            savedWallet.setPendingBalance(pendingBalance);
            realm.insertOrUpdate(savedWallet);
        });

    }

    public void clear() {
        if (realm != null) {
            realm.executeTransaction(realm -> realm.deleteAll());
        }
    }
}