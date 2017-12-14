/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;


import com.google.gson.annotations.SerializedName;

import io.multy.model.entities.Output;
import io.realm.RealmList;
import io.realm.RealmObject;

public class WalletAddress extends RealmObject {

    @SerializedName("addressindex")
    private int index;
    @SerializedName("address")
    private String address;
    @SerializedName("amount")
    private int amount;
    @SerializedName("spendableoutputs")
    private RealmList<Output> outputs;

    public WalletAddress() {
    }

    public WalletAddress(int index, String address) {
        this.index = index;
        this.address = address;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public RealmList<Output> getOutputs() {
        return outputs;
    }

    public void setOutputs(RealmList<Output> outputs) {
        this.outputs = outputs;
    }

    @Override
    public String toString() {
        return "WalletAddress{" +
                "index=" + index +
                ", address='" + address + '\'' +
                ", amount=" + amount +
                '}';
    }
}
