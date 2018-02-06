/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.requests;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anschutz1927@gmail.com on 12.01.18.
 */

public class UpdateWalletNameRequest {

    @SerializedName("walletname")
    private String newName;
    @SerializedName("currencyID")
    private int currencyId;
    @SerializedName("walletIndex")
    private int walletIndex;

    public UpdateWalletNameRequest(String newName, int currencyId, int walletIndex) {
        this.newName = newName;
        this.currencyId = currencyId;
        this.walletIndex = walletIndex;
    }

    public String getNewName() {
        return newName;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public int getWalletIndex() {
        return walletIndex;
    }

    @Override
    public String toString() {
        return "UpdateWalletNameRequest{" +
                "newName=" + newName +
                '}';
    }
}
