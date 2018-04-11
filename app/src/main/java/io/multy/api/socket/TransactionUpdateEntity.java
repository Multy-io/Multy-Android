/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api.socket;

import com.google.gson.annotations.SerializedName;

public class TransactionUpdateEntity {

    @SerializedName("transactionType")
    private int type;
    @SerializedName("amount")
    private double amount;
    @SerializedName("txid")
    private String txId;
    @SerializedName("address")
    private String address;

    public int getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getTxId() {
        return txId;
    }

    public String getAddress() {
        return address;
    }
}
