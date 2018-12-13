/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.core;

import com.google.gson.annotations.SerializedName;

public class Account {

    public static final int ACCOUNT_TYPE_DEFAULT = 0;

    @SerializedName("type")
    private int type = ACCOUNT_TYPE_DEFAULT;
    @SerializedName("private_key")
    private String privateKey;

    public Account(int type, String privateKey) {
        this.type = type;
        this.privateKey = privateKey;
    }

    public Account() {
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
