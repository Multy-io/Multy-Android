/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.core;

import com.google.gson.annotations.SerializedName;

public class Payload {

    @SerializedName("balance")
    private String balance;
    @SerializedName("destination_address")
    private String destinationAddress;
    @SerializedName("destination_amount")
    private String destinationAmount;

    public Payload(String balance, String destinationAddress, String destinationAmount) {
        this.balance = balance;
        this.destinationAddress = destinationAddress;
        this.destinationAmount = destinationAmount;
    }

    public Payload() {
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public void setDestinationAmount(String destinationAmount) {
        this.destinationAmount = destinationAmount;
    }
}
