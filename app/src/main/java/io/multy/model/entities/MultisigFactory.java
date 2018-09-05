/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MultisigFactory extends RealmObject {

        @PrimaryKey
        private int id = 0;

        @SerializedName("ethmainnet")
        private String ethMainNet;

        @SerializedName("ethtestnet")
        private String ethTestNet;

        public String getEthMainNet() {
            return ethMainNet;
        }

        public String getEthTestNet() {
            return ethTestNet;
        }
    }