/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

public class Erc20TokenPrice {

    @SerializedName("rate")
    private String rate;
    @SerializedName("currency")
    private String currency;
    @SerializedName("diff")
    private String diff;
    @SerializedName("diff7d")
    private String diff7d;
    @SerializedName("diff30d")
    private String diff30d;
    @SerializedName("marketCapUsd")
    private String marketCapUsd;
    @SerializedName("availableSupply")
    private String availableSupply;
    @SerializedName("volume24h")
    private String volume24h;
    @SerializedName("ts")
    private String timestamp;



    public String getRate() {
        return rate;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDiff() {
        return diff;
    }

    public String getDiff7d() {
        return diff7d;
    }

    public String getDiff30d() {
        return diff30d;
    }

    public String getMarketCapUsd() {
        return marketCapUsd;
    }

    public String getAvailableSupply() {
        return availableSupply;
    }

    public String getVolume24h() {
        return volume24h;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
