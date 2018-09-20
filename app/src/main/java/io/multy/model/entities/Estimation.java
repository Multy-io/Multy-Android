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
    private String confirmTransaction;
    @SerializedName("deployMultisig")
    private String deployMultisig;
    @SerializedName("priceOfCreation")
    private String priceOfCreation;
    @SerializedName("revokeConfirmation")
    private String revokeConfirmation;
    @SerializedName("submitTransaction")
    private String submitTransaction;

    public String getConfirmTransaction() {
        return confirmTransaction;
    }

    public void setConfirmTransaction(String confirmTransaction) {
        this.confirmTransaction = confirmTransaction;
    }

    public String getDeployMultisig() {
        return deployMultisig;
    }

    public void setDeployMultisig(String deployMultisig) {
        this.deployMultisig = deployMultisig;
    }

    public String getPriceOfCreation() {
        return priceOfCreation;
    }

    public void setPriceOfCreation(String priceOfCreation) {
        this.priceOfCreation = priceOfCreation;
    }

    public String getRevokeConfirmation() {
        return revokeConfirmation;
    }

    public void setRevokeConfirmation(String revokeConfirmation) {
        this.revokeConfirmation = revokeConfirmation;
    }

    public String getSubmitTransaction() {
        return submitTransaction;
    }

    public void setSubmitTransaction(String submitTransaction) {
        this.submitTransaction = submitTransaction;
    }
}