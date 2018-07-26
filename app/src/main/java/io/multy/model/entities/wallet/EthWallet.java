/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import io.multy.R;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;


public class EthWallet extends RealmObject {

    public final static BigDecimal DIVISOR = new BigDecimal(Math.pow(10, 18));
    public final static BigInteger DIVISOR_GWEI = BigInteger.valueOf((long) Math.pow(10, 9));
    public final static String PENDING_BALANCE = "pendingBalance";

    @SerializedName("nonce")
    private String nonce = "0";
    @SerializedName("balance")
    private String balance = "0";
    @SerializedName("addresses")
    private RealmList<WalletAddress> addresses;
    @SerializedName("pendingbalance")
    private String pendingBalance = "0";

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getPendingBalance() {
        return pendingBalance;
    }

    public void setPendingBalance(String pendingBalance) {
        this.pendingBalance = pendingBalance;
    }

    public RealmList<WalletAddress> getAddresses() {
        return addresses;
    }

    public String calculateAvailableBalance(String balance) {
        if (balance == null) {
            return "0";
        }

        return balance;
    }

    public void setAddresses(RealmList<WalletAddress> addresses) {
        this.addresses = addresses;
    }

    public EthWallet asRealmObject(Realm realm) {
        EthWallet ethWallet = realm.createObject(EthWallet.class);
        ethWallet.setAddresses(new RealmList<>());

        for (WalletAddress walletAddress : getAddresses()) {
            ethWallet.getAddresses().add(realm.copyToRealm(walletAddress));
        }
        ethWallet.setNonce(nonce);
        ethWallet.setPendingBalance(pendingBalance);
        ethWallet.setBalance(balance);
        return ethWallet;
    }

    public static double getTransactionPrice(long gasPrice) {
        return CryptoFormatUtils.weiToEth(String.valueOf(Long.valueOf(Constants.GAS_LIMIT_DEFAULT) * gasPrice));
    }

    public BigInteger getPendingBalanceNumeric() {
        return new BigInteger(pendingBalance);
    }

    public String getPendingBalanceLabel() {
        return CryptoFormatUtils.weiToEthLabel(pendingBalance);
    }

    public String getFiatPendingBalanceLabel() {
        return CryptoFormatUtils.weiToUsd(getPendingBalanceNumeric());
    }
}
