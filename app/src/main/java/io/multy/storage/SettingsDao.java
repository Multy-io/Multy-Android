/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import java.util.List;

import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.DeviceId;
import io.multy.model.entities.DonateFeatureEntity;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.RootKey;
import io.multy.model.entities.Token;
import io.multy.model.entities.UserId;
import io.multy.model.responses.ServerConfigResponse;
import io.multy.util.NativeDataHelper;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsDao {

    private Realm realm;

    public SettingsDao(@NonNull Realm realm) {
        this.realm = realm;
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
        return realm != null ? realm.where(UserId.class).findFirst() : null;
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

    public void setUserId(UserId userId) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(userId));
    }

    public void setByteSeed(ByteSeed byteSeed) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(byteSeed));
    }

    public ByteSeed getByteSeed() {
        return realm.where(ByteSeed.class).findFirst();
    }

    public void saveCurrenciesRate(CurrenciesRate currenciesRate, Realm.Transaction.OnSuccess onSuccess) {
        realm.executeTransactionAsync(realm -> realm.insertOrUpdate(currenciesRate), onSuccess);
    }

    @Nullable
    public CurrenciesRate getCurrenciesRate() {
        CurrenciesRate currenciesRate = realm.where(CurrenciesRate.class).findFirst();
        if (currenciesRate == null) {
            currenciesRate = new CurrenciesRate();
        }
        return currenciesRate;
    }

    @Nullable
    public double getCurrenciesRateById(int currencyId) {
        CurrenciesRate currenciesRate = realm.where(CurrenciesRate.class).findFirst();
        if (currenciesRate == null) {
            return 0;
        }
        switch (NativeDataHelper.Blockchain.valueOf(currencyId)) {
            case BTC:
                return currenciesRate.getBtcToUsd();
            case ETH:
                return currenciesRate.getEthToUsd();
                default: return 0;
        }
    }

    public void saveDonation(List<ServerConfigResponse.Donate> donates) {
        realm.executeTransactionAsync(realm -> {
            for (ServerConfigResponse.Donate donate : donates) {
                DonateFeatureEntity donateFeature = new DonateFeatureEntity(donate.getFeatureCode());
                donateFeature.setDonationAddress(donate.getDonationAddress());
                realm.insertOrUpdate(donateFeature);
            }
        }, Throwable::printStackTrace);
    }

    public RealmResults<DonateFeatureEntity> getDonationAddresses() {
        return realm.where(DonateFeatureEntity.class).findAll();
    }

    public String getDonationAddress(int donationCode) {
        DonateFeatureEntity donateFeature = realm.where(DonateFeatureEntity.class)
                .equalTo(DonateFeatureEntity.FEATURE_CODE, donationCode).findFirst();
        return donateFeature == null ? null : donateFeature.getDonationAddress();
    }

    public DonateFeatureEntity getDonationFeature(String address) {
        return realm.where(DonateFeatureEntity.class)
                .equalTo(DonateFeatureEntity.DONATION_ADDRESS, address).findFirst();
    }
}
