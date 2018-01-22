/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.socket;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    public enum TransactionType {
        @SerializedName("outcoming")
        OUTCOMING,
        @SerializedName("incoming")
        INCOMING
    }

    @SerializedName("transactionType")
    private TransactionType transactionType;
    private int amount;
    @SerializedName("txid")
    private int transactionId;

    public Transaction(TransactionType transactionType, int amount, int transactionId) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionId = transactionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
}
