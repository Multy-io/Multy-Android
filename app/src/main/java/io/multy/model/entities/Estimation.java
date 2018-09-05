/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anschutz1927@gmail.com on 04.09.18.
 */
public class Estimation {

    @SerializedName("confirmTransaction")
    private long confirmTransaction;
    @SerializedName("deployMultisig")
    private long deployMultisig;
    @SerializedName("priceOfCreation")
    private long priceOfCreation;
    @SerializedName("revokeConfirmation")
    private long revokeConfirmation;
    @SerializedName("submitTransaction")
    private long submitTransaction;

    public long getConfirmTransaction() {
        return confirmTransaction;
    }

    public void setConfirmTransaction(long confirmTransaction) {
        this.confirmTransaction = confirmTransaction;
    }

    public long getDeployMultisig() {
        return deployMultisig;
    }

    public void setDeployMultisig(long deployMultisig) {
        this.deployMultisig = deployMultisig;
    }

    public long getPriceOfCreation() {
        return priceOfCreation;
    }

    public void setPriceOfCreation(long priceOfCreation) {
        this.priceOfCreation = priceOfCreation;
    }

    public long getRevokeConfirmation() {
        return revokeConfirmation;
    }

    public void setRevokeConfirmation(long revokeConfirmation) {
        this.revokeConfirmation = revokeConfirmation;
    }

    public long getSubmitTransaction() {
        return submitTransaction;
    }

    public void setSubmitTransaction(long submitTransaction) {
        this.submitTransaction = submitTransaction;
    }
}