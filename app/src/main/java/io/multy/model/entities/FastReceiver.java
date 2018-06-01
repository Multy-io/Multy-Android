/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

import io.multy.Multy;

public class FastReceiver {

    @SerializedName("userid")
    private String id;
    @SerializedName("usercode")
    private String code;
    @SerializedName("currencyid")
    private int currencyId;
    @SerializedName("networkid")
    private int networkId;
    @SerializedName("address")
    private String address;
    @SerializedName("amount")
    private String amount;

    public FastReceiver(String id, String code, int currencyId, int networkId, String address, String amount) {
        this.id = id;
        this.code = code;
        this.currencyId = currencyId;
        this.networkId = networkId;
        this.address = address;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }

    public int getNetworkId() {
        return networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public static int getImageResId(final String address) {
        int counter = 0;
        for (char ch : address.toCharArray()) {
            counter += (int) ch;
        }
        return Multy.getContext().getResources().getIdentifier("ic_" + ((counter % 20) + 1), "drawable", Multy.getContext().getPackageName());
    }
}
