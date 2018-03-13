/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api.socket;

import com.google.gson.annotations.SerializedName;

public class TransactionUpdateResponse {

    @SerializedName("NotificationMsg")
    private TransactionUpdateEntity entity;

    public TransactionUpdateEntity getEntity() {
        return entity;
    }
}
