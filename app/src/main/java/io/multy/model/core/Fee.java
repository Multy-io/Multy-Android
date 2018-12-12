/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.core;

import com.google.gson.annotations.SerializedName;

public class Fee {

    @SerializedName("gas_price")
    private String gas_price;
    @SerializedName("gas_limit")
    private String gasLimit;

    public Fee() {
    }

    public Fee(String gas_price, String gasLimit) {
        this.gas_price = gas_price;
        this.gasLimit = gasLimit;
    }

    public void setGas_price(String gas_price) {
        this.gas_price = gas_price;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }
}
