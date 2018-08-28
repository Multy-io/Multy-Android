/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

import io.multy.util.NumberFormatter;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class EosWallet extends RealmObject {

    public final static BigDecimal DIVISOR = new BigDecimal(Math.pow(10, 4));

    @SerializedName("pendingbalance")
    private String pendingBalance = "0";
    @SerializedName("balance")
    private String balance = "0";
    @SerializedName("ownerkey")
    private String publicKey;
    private String privateKey;

    @SerializedName("addresses")
    private RealmList<WalletAddress> addresses;

    public String getPendingBalance() {
        return pendingBalance;
    }

    public void setPendingBalance(String pendingBalance) {
        this.pendingBalance = pendingBalance;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public RealmList<WalletAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(RealmList<WalletAddress> addresses) {
        this.addresses = addresses;
    }

    public String getDividedBalance(String balance) {
        return NumberFormatter.getEosInstance().format(new BigDecimal(balance).divide(DIVISOR));
    }

    public EosWallet asRealmObject(Realm realm) {
        EosWallet eosWallet = realm.createObject(EosWallet.class);
        eosWallet.setAddresses(new RealmList<>());

        for (WalletAddress walletAddress : getAddresses()) {
            eosWallet.getAddresses().add(realm.copyToRealm(walletAddress));
        }

        eosWallet.setPendingBalance(pendingBalance);
        eosWallet.setBalance(balance);
        eosWallet.setPublicKey(publicKey);
        eosWallet.setPrivateKey(privateKey);
        return eosWallet;
    }
}
