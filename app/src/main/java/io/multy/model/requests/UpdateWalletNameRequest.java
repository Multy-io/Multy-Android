/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.requests;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anschutz1927@gmail.com on 12.01.18.
 */

public class UpdateWalletNameRequest {

    @SerializedName("walletname")
    private String newName;

    public UpdateWalletNameRequest(String newName) {
        this.newName = newName;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public String toString() {
        return "UpdateWalletNameRequest{" +
                "newName=" + newName +
                '}';
    }
}
