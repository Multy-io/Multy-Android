/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import io.multy.model.entities.Rates;

public class FeeRatesResponse {

    @SerializedName("speeds")
    private Rates rates;

    public Rates getRates() {
        return rates;
    }
}
