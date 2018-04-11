/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import io.multy.model.entities.TransactionHistory;

public class TransactionHistoryResponse {

    @SerializedName("history")
    private ArrayList<TransactionHistory> histories;

    public ArrayList<TransactionHistory> getHistories() {
        return histories;
    }
}
