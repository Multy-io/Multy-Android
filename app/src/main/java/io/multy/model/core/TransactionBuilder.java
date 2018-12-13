/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.core;

import com.google.gson.annotations.SerializedName;

public class TransactionBuilder {

    @SerializedName("blockchain")
    private String blockChain;
    @SerializedName("net_type")
    private int netType;
    @SerializedName("account")
    private Account account;
    @SerializedName("builder")
    private Builder builder;
    @SerializedName("transaction")
    private Transaction transaction;

    public TransactionBuilder() {
    }

    public TransactionBuilder(String blockChain, int netType, Account account, Builder builder, Transaction transaction) {
        this.blockChain = blockChain;
        this.netType = netType;
        this.account = account;
        this.builder = builder;
        this.transaction = transaction;
    }

    public void setBlockChain(String blockChain) {
        this.blockChain = blockChain;
    }

    public void setNetType(int netType) {
        this.netType = netType;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
