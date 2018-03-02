/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.content.Context;

import com.samwolfand.oneprefs.Prefs;

import java.util.List;

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
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

@Deprecated
public class DatabaseHelper {

    private Realm realm;

    public DatabaseHelper(Context context) {
        try {
            realm = Realm.getInstance(RealmManager.getConfiguration(context));
        } catch (Exception exception) {
            exception.printStackTrace();
            Analytics.getInstance(context).logEvent(AnalyticsConstants.ERROR_DATABASE_DECRYPT,
                    AnalyticsConstants.ERROR_DATABASE_DECRYPT,
                    exception.getMessage().replaceAll(" ", "_"));
        }
    }

    public RealmResults<WalletRealmObject> getWallets() {
        return realm.where(WalletRealmObject.class).findAll();
    }

    public void saveWallet(WalletRealmObject wallet) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(wallet));
    }

    public void saveWallets(List<WalletRealmObject> wallets) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(wallets));
    }

    public void saveAmount(WalletRealmObject wallet, double amount) {
        realm.executeTransaction(realm -> {
            wallet.setBalance(amount);
            realm.insertOrUpdate(wallet);
        });
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

    public void saveCurrenciesRate(CurrenciesRate currenciesRate) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(currenciesRate));
    }

    public CurrenciesRate getCurrenciesRate() {
        return realm != null ? realm.where(CurrenciesRate.class).findFirst() : null;
    }

    public WalletAddress getWalletAddress(int id) {
        return realm.where(WalletAddress.class).equalTo("index", id).findFirst();
    }

    public void removeWallet(int id) {
        WalletRealmObject wallet = getWalletById(id);
        realm.executeTransaction(realm -> wallet.deleteFromRealm());
    }
}