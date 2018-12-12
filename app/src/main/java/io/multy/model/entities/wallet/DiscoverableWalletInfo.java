/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;


import com.google.gson.annotations.SerializedName;

import io.multy.model.entities.Erc20Balance;
import io.multy.model.entities.Output;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DiscoverableWalletInfo {

    @SerializedName("walletIndex")
    private int walletIndex;
    @SerializedName("addressIndex")
    private int addressIndex;
    @SerializedName("address")
    private String address;
    @SerializedName("walletName")
    private String name;

    public DiscoverableWalletInfo(int walletIndex, int addressIndex, String address, String name) {
        this.walletIndex = walletIndex;
        this.addressIndex = addressIndex;
        this.address = address;
        this.name = name;
    }
}
