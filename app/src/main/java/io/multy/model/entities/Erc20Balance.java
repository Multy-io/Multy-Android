/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Erc20Balance extends RealmObject {
    @SerializedName("Address")
    @PrimaryKey
    private String address;
    @SerializedName("Balance")
    private String balance;
}