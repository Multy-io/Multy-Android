/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import java.util.List;
import java.util.Objects;

import io.multy.model.entities.wallet.RecentAddress;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.util.NativeDataHelper;
import io.reactivex.annotations.NonNull;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class AssetsDao {

    private Realm realm;

    public AssetsDao(@NonNull Realm realm) {
        this.realm = realm;
    }

    public void saveWallet(Wallet wallet) {
        realm.executeTransaction(realm -> saveSingleWallet(wallet));
    }

    public void saveWallets(List<Wallet> wallets) {
        realm.executeTransaction(realm -> {
            for (Wallet wallet : wallets) {

                Wallet toDelete = getWalletById(wallet.getId());
                if (toDelete != null) {
                    toDelete.deleteFromRealm(); //TODO review this
                }

                saveSingleWallet(wallet);
            }
        });
    }

    private void saveSingleWallet(Wallet wallet) {
        final int index = wallet.getIndex();
        final String name = wallet.getWalletName();
        final String balance = wallet.getBalance();

        Wallet savedWallet = new Wallet();
        savedWallet.setDateOfCreation(wallet.getDateOfCreation());
        savedWallet.setLastActionTime(wallet.getLastActionTime());
        savedWallet.setIndex(index);
        savedWallet.setWalletName(name);
        savedWallet.setBalance(balance);
        savedWallet.setNetworkId(wallet.getNetworkId());
        savedWallet.setCurrencyId(wallet.getCurrencyId());

        if (wallet.getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()) {
            savedWallet.setBtcWallet(wallet.getBtcWallet().asRealmObject(realm));
            savedWallet.setBalance(String.valueOf(savedWallet.getBtcWallet().calculateBalance()));
            savedWallet.setAvailableBalance(String.valueOf(savedWallet.getBtcWallet().calculateAvailableBalance()));
        } else {
            savedWallet.setEthWallet(Objects.requireNonNull(wallet.getEthWallet()).asRealmObject(realm));
            final String ethBalance = savedWallet.getEthWallet().getBalance();
            final String ethPendingBalance = savedWallet.getEthWallet().getPendingBalance();
            final String ethAvailableBalance = savedWallet.getEthWallet().calculateAvailableBalance(ethBalance);

            savedWallet.setBalance(ethBalance);
            savedWallet.setAvailableBalance(ethAvailableBalance);
        }

        realm.insertOrUpdate(savedWallet);
    }

    public RealmResults<Wallet> getWallets() {
        return realm.where(Wallet.class).sort("lastActionTime", Sort.ASCENDING).findAll();
    }

    public RealmResults<Wallet> getWallets(int blockChainId) {
        return realm.where(Wallet.class).equalTo("currencyId", blockChainId).findAll();
    }

    public RealmResults<Wallet> getWallets(int blockChainId, int networkId) {
        return realm.where(Wallet.class).equalTo("currencyId", blockChainId)
                .equalTo("networkId", networkId).findAll();
    }

    public void saveBtcAddress(long id, WalletAddress address) {
        realm.executeTransaction(realm -> {
            Wallet wallet = getWalletById(id);
            wallet.getBtcWallet().getAddresses().add(realm.copyToRealm(address));
            realm.insertOrUpdate(wallet);
        });
    }

    public void delete(@NonNull final RealmObject object) {
        realm.executeTransaction(realm -> object.deleteFromRealm());
    }

    public void deleteAll() {
        realm.executeTransaction(realm -> realm.where(Wallet.class).findAll().deleteAllFromRealm());
    }

    public Wallet getWalletById(long id) {
        return realm.where(Wallet.class).equalTo("dateOfCreation", id).findFirst();
    }

    public void updateWalletName(long id, String newName) {
        realm.executeTransaction(realm1 -> {
            Wallet wallet = getWalletById(id);
            wallet.setWalletName(newName);
            realm1.insertOrUpdate(wallet);
        });
    }

    public void removeWallet(long id) {
        realm.executeTransaction(realm -> {
            Wallet wallet = getWalletById(id);
            wallet.deleteFromRealm();
        });
    }

    public void saveRecentAddress(RecentAddress recentAddress) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(recentAddress));
    }

    public RealmResults<RecentAddress> getRecentAddresses() {
        return realm.where(RecentAddress.class).findAll();
    }

    public RealmResults<RecentAddress> getRecentAddresses(int currencyId, int networkId) {
        return realm.where(RecentAddress.class).equalTo(RecentAddress.CURRENCY_ID, currencyId)
                .equalTo(RecentAddress.NETWORK_ID, networkId).findAll();
    }

    public boolean ifAddressExist(long addressTo) {
        RealmQuery<RecentAddress> query = realm.where(RecentAddress.class).equalTo(RecentAddress.RECENT_ADDRESS_ID, addressTo);
        return query.count() != 0;
    }
}
