/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api.socket;

import com.google.gson.annotations.SerializedName;

public class GraphPoint {

    @SerializedName("date")
    private long date;
    @SerializedName("price")
    private float price;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
