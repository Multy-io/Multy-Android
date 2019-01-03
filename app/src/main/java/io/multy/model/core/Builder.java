/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.core;

import com.google.gson.annotations.SerializedName;

public class Builder {

    public static final String TYPE_BASIC = "basic";
    public static final String TYPE_ERC20 = "erc20";
    public static final String ACTION_TRANSFER = "transfer";

    @SerializedName("type")
    private String type;
    @SerializedName("payload")
    private Payload payload;
    @SerializedName("action")
    private String action;

    public Builder() {
    }

    public Builder(String type, String action, Payload payload) {
        this.type = type;
        this.action = action;
        this.payload = payload;
    }

    public Builder(String type, Payload payload) {
        this.type = type;
        this.payload = payload;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
