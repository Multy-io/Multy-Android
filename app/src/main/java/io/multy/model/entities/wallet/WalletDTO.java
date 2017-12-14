/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WalletDTO {

    @SerializedName("walletindex")
    private int index;
    @SerializedName("verboseaddresses")
    private List<WalletAddress> addresses;
}
