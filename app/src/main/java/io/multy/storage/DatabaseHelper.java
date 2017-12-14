/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.content.Context;

import io.multy.encryption.MasterKeyGenerator;
import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.DeviceId;
import io.multy.model.entities.ExchangePrice;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.Output;
import io.multy.model.entities.RootKey;
import io.multy.model.entities.Token;
import io.multy.model.entities.UserId;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.util.MyRealmMigration;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

public class DatabaseHelper {

    private Realm realm;

    public DatabaseHelper(Context context) {
        realm = Realm.getDefaultInstance();
    }

    private RealmConfiguration getRealmConfiguration(Context context) {
        if (MasterKeyGenerator.generateKey(context) != null) {
            return new RealmConfiguration.Builder()
//                    .encryptionKey(MasterKeyGenerator.generateKey(context))
                    .schemaVersion(1)
                    .migration(new MyRealmMigration())
                    .build();
        } else {
            return new RealmConfiguration.Builder().build();
        }
    }

    public RealmResults<WalletRealmObject> getWallets() {
        return realm.where(WalletRealmObject.class).findAll();
    }

    public void saveWallet(WalletRealmObject wallet) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(wallet));
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

    public void updateWallet(int index, RealmList<WalletAddress> addresses, double balance) {
        realm.executeTransaction(realm -> {
            WalletRealmObject savedWallet = getWalletById(index);
            if (savedWallet != null) {
                if (!addresses.isManaged()) {
                    RealmList<WalletAddress> managedAddresses = new RealmList<>();
                    RealmList<Output> managedOutputs = new RealmList<>();
                    WalletAddress managedAddress;
                    Output managedOutput;

                    for (WalletAddress walletAddress : addresses) {
                        managedAddress = realm.createObject(WalletAddress.class);
                        managedAddress.setAddress(walletAddress.getAddress());
                        managedAddress.setAmount(walletAddress.getAmount());
                        managedAddress.setIndex(walletAddress.getIndex());

                        if (walletAddress.getOutputs() != null && walletAddress.getOutputs().size() > 0 && !walletAddress.isManaged()) {
                            for (Output output : walletAddress.getOutputs()) {
                                managedOutput = realm.createObject(Output.class);
                                managedOutput.setTxId(output.getTxId());
                                managedOutput.setTxOutAmount(output.getTxOutAmount());
                                managedOutput.setTxOutScript(output.getTxOutScript());
                                managedOutput.setTxOutId(output.getTxOutId());
                            }
                            managedAddress.setOutputs(managedOutputs);
                        }
                    }

                    savedWallet.setAddresses(managedAddresses);
                }
                savedWallet.setBalance(balance);
                realm.insertOrUpdate(savedWallet);
            }
        });

    }
}