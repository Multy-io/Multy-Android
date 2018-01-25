/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import java.util.List;

import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.reactivex.annotations.NonNull;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class AssetsDao {

    private Realm realm;

    public AssetsDao(@NonNull Realm realm) {
        this.realm = realm;
    }

    public void saveWallet(WalletRealmObject wallet) {
        realm.executeTransaction(realm -> saveSingleWallet(wallet));
    }

    public void saveWallets(List<WalletRealmObject> wallets) {
        realm.executeTransaction(realm -> {
            for (WalletRealmObject wallet : wallets) {
                saveSingleWallet(wallet);
            }
        });
    }

    private void saveSingleWallet(WalletRealmObject wallet) {
        final int index = wallet.getWalletIndex();
        final String name = wallet.getName();
        final double balance = wallet.calculateBalance();
        final double pendingBalance = wallet.calculatePendingBalance();


        WalletRealmObject savedWallet = getWalletById(index);
        if (savedWallet == null) {
            savedWallet = new WalletRealmObject();
            savedWallet.setWalletIndex(index);
        }
        savedWallet.setName(name);
        savedWallet.setAddresses(new RealmList<>());
        for (WalletAddress walletAddress : wallet.getAddresses()) {
            savedWallet.getAddresses().add(realm.copyToRealm(walletAddress));
        }
        savedWallet.setBalance(balance);
        savedWallet.setPendingBalance(pendingBalance);
        realm.insertOrUpdate(savedWallet);
    }

    public RealmResults<WalletRealmObject> getWallets() {
        return realm.where(WalletRealmObject.class).findAll();
    }

    public void saveAddress(int walletIndex, WalletAddress address) {
        realm.executeTransaction(realm -> {
            WalletRealmObject walletRealmObject = getWalletById(walletIndex);
            walletRealmObject.getAddresses().add(realm.copyToRealm(address));
            realm.insertOrUpdate(walletRealmObject);
        });
    }

    public void delete(@NonNull final RealmObject object) {
        realm.executeTransaction(realm -> object.deleteFromRealm());
    }

    public void deleteAll() {
        realm.executeTransaction(realm -> realm.where(WalletRealmObject.class).findAll().deleteAllFromRealm());
    }

    public WalletRealmObject getWalletById(int id) {
        return realm.where(WalletRealmObject.class).equalTo("walletIndex", id).findFirst();
    }

    public void updateWalletName(int id, String newName) {
        realm.executeTransaction(realm1 -> {
            WalletRealmObject wallet = getWalletById(id);
            wallet.setName(newName);
            realm1.insertOrUpdate(wallet);
        });
    }

    public void removeWallet(int id) {
        realm.executeTransaction(realm -> {
            WalletRealmObject wallet = getWalletById(id);
            wallet.deleteFromRealm();
        });
    }
}
