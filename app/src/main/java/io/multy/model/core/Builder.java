/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.core;

import com.google.gson.annotations.SerializedName;

public class Builder {

    public static final String TYPE_BASIC = "basic";

    @SerializedName("type")
    private String type;
    @SerializedName("payload")
    private Payload payload;

    public Builder() {
    }

    public Builder(String type, Payload payload) {
        this.type = type;
        this.payload = payload;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
