/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

public class AddAddressRequestEntity {

    @SerializedName("walletIndex")
    private int walletIndex;
    @SerializedName("address")
    private String address;
    @SerializedName("addressIndex")
    private int addressIndex;
}
