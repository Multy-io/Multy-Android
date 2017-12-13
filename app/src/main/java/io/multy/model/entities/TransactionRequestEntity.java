/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

public class TransactionRequestEntity {

    @SerializedName("transaction")
    private String transaction;

    @SerializedName("allowHighFees")
    private boolean allowHighFees;

    public TransactionRequestEntity(String transaction, boolean allowHighFees) {
        this.transaction = transaction;
        this.allowHighFees = allowHighFees;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public boolean isAllowHighFees() {
        return allowHighFees;
    }

    public void setAllowHighFees(boolean allowHighFees) {
        this.allowHighFees = allowHighFees;
    }
}
