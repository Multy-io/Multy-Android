/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.core;

import com.google.gson.annotations.SerializedName;

public class TransactionResponse {

    @SerializedName("transaction")
    private Transaction transaction;

    public String getTransactionHex() {
        return transaction == null ? "" : transaction.hex;
    }

    private class Transaction {
        @SerializedName("serialized")
        String hex;
    }
}
