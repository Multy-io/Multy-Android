/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anschutz1927@gmail.com on 05.09.18.
 */
public class TransactionOwner {

    @SerializedName("address")
    private String address;
    @SerializedName("confirmationStatus")
    private int confirmationStatus;
    @SerializedName("confirmationTime")
    private long confirmationTime;
    @SerializedName("confirmationtx")
    private String confirmationTx;
    @SerializedName("seenTime")
    private long seenTime;

    public String getAddress() {
        return address;
    }

    public int getConfirmationStatus() {
        return confirmationStatus;
    }

    public long getConfirmationTime() {
        return confirmationTime;
    }

    public String getConfirmationTx() {
        return confirmationTx;
    }

    public long getSeenTime() {
        return seenTime;
    }
}
