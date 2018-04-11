/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.util.NativeDataHelper;
import io.multy.util.WalletDeserializer;

public class TestWalletResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @JsonAdapter(WalletDeserializer.class)
    @SerializedName("wallets")
    private List<Wallet> wallets;

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

    public List<Wallet> getWallets() {
        return wallets;
    }

    public void setWallets(List<Wallet> wallets) {
        this.wallets = wallets;
    }
}
