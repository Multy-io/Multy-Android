/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

import io.multy.model.responses.AuthResponse;
import io.realm.RealmObject;

public class Token extends RealmObject {

    @SerializedName("token")
    private String token;
    @SerializedName("expire")
    private String expire;

    public Token() {
    }

    public Token(AuthResponse authResponse) {
        this.token = authResponse.getToken();
        this.expire = authResponse.getExpireDateString();
    }

    public String getToken() {
        return token;
    }

    public String getExpire() {
        return expire;
    }
}
