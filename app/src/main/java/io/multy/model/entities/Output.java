/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class Output extends RealmObject{

    @SerializedName("txid")
    private String txId;
    @SerializedName("txoutid")
    private int txOutId;
    @SerializedName("txoutamount")
    private String txOutAmount;
    @SerializedName("txoutscript")
    private String txOutScript;

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
}
