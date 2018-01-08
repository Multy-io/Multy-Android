/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class Output extends RealmObject{

    public static final String ICOMCING_MEMPOOL = "incoming in mempool";
    public static final String INCOMING_BLOCK = "incoming in block";
    public static final String OUTCOMING_MEMPOOL = "spend in mempool";
    public static final String OUTCOMING_BLOCK = "spend in block";
    public static final String IN_BLOCK_CONFIRMED = "in block confirmed";
    public static final String REJECTED_FROM_BLOCK = "rejected block";

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
    private String status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
