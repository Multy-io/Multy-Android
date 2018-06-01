/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import io.multy.model.entities.NotificationMessage;

public class TransactionUpdateResponse {

    @SerializedName("NotificationMsg")
    NotificationMessage notificationMessage;
}
