/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class Output extends RealmObject{

    private int addressId;
    @SerializedName("txid")
    private String txId;
    @SerializedName("txoutid")
    private int txOutId;
    @SerializedName("txoutamount")
    private String txOutAmount;
    @SerializedName("txoutscript")
    private String txOutScript;
    @SerializedName("txstatus")
    private int status;
    @SerializedName("addressindex")
    private String addressIndex;

    private boolean pending;

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public int getTxOutId() {
        return txOutId;
    }

    public void setTxOutId(int txOutId) {
        this.txOutId = txOutId;
    }

    public String getTxOutAmount() {
        return txOutAmount;
    }

    public void setTxOutAmount(String txOutAmount) {
        this.txOutAmount = txOutAmount;
    }

    public String getTxOutScript() {
        return txOutScript;
    }

    public void setTxOutScript(String txOutScript) {
        this.txOutScript = txOutScript;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAddressIndex() {
        return addressIndex;
    }

    public void setAddressIndex(String addressIndex) {
        this.addressIndex = addressIndex;
    }

    @Override
    public String toString() {
        return "Output{" +
                "txId='" + txId + '\'' +
                ", txOutId=" + txOutId +
                ", txOutAmount='" + txOutAmount + '\'' +
                ", txOutScript='" + txOutScript + '\'' +
                ", pending=" + pending +
                '}';
    }
}
