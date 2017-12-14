/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.multy.model.entities.wallet.WalletRealmObject;

public class WalletsResponse {

    @SerializedName("code")
    private int code;
    @SerializedName("message")
    private String message;
    @SerializedName("wallets")
    private List<WalletRealmObject> wallets;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<WalletRealmObject> getWallets() {
        return wallets;
    }

    public void setWallets(List<WalletRealmObject> wallets) {
        this.wallets = wallets;
    }
}
