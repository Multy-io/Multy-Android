/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ExchangePairResponse {
    @SerializedName("code")
    private int code;
    @SerializedName("message")
    private String message;
    @SerializedName("amount")
    private float amount;

    @SerializedName("transactionId")
    private String transactionId;

    @SerializedName("payinAddress")
    private String payToAddress;

    @SerializedName("payoutAddress")
    private String receiveToAddress;



    public void setCode(int code) {
        this.code = code;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setAmount(float amount) {
        this.amount = amount;
    }
    public void setTransactionId(String id) {this.transactionId = id;}
    public void setPayToAddress(String address) {this.payToAddress = address;}
    public void setReceiveToAddress(String address) {this.receiveToAddress = address;}

    public String getMessage() {
        return message;
    }
    public int getCode() {
        return code;
    }
    public float getAmount() {
        return amount;
    }
    public String getTransactionId() {return this.transactionId;}
    public String getPayToAddress() {return this.payToAddress;}
    public String getReceiveToAddress() {return this.receiveToAddress;}



}
