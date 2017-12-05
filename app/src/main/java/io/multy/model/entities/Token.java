/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

public class Token {

    @SerializedName("token")
    private String token;
    @SerializedName("expire")
    private String expire;

    public String getToken() {
        return token;
    }

    public String getExpire() {
        return expire;
    }
}
