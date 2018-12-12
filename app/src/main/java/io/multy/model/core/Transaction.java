/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.core;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("nonce")
    private String nonce;
    @SerializedName("fee")
    private Fee fee;

    public Transaction() {
    }

    public Transaction(String nonce, Fee fee) {
        this.nonce = nonce;
        this.fee = fee;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }
}
