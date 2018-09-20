/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class Owner extends RealmObject {

    @SerializedName("userid")
    private String userId;
    @SerializedName("address")
    private String address;
    @SerializedName("associated")
    private boolean associated;
    @SerializedName("creator")
    private boolean creator;
    @SerializedName("walletIndex")
    private int walletIndex;
    @SerializedName("addressIndex")
    private int addressIndex;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isAssociated() {
        return associated;
    }

    public void setAssociated(boolean associated) {
        this.associated = associated;
    }

    public boolean isCreator() {
        return creator;
    }

    public void setCreator(boolean creator) {
        this.creator = creator;
    }

    public int getWalletIndex() {
        return walletIndex;
    }

    public void setWalletIndex(int walletIndex) {
        this.walletIndex = walletIndex;
    }

    public int getAddressIndex() {
        return addressIndex;
    }

    public void setAddressIndex(int addressIndex) {
        this.addressIndex = addressIndex;
    }
}
