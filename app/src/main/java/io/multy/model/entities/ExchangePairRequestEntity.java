/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

public class ExchangePairRequestEntity {


    @SerializedName("amount")
    private String amount;

    @SerializedName("to")
    private String to;

    @SerializedName("from")
    private String from;

    @SerializedName("address")
    private String address;

//    @SerializedName("address")
//    private String sendFromAddress;

    public ExchangePairRequestEntity(ExchangePair pair) {
        this.from = pair.getFromAsset();
        this.to = pair.getToAsset();
        this.amount = String.valueOf(pair.getRate());
        this.address = pair.getReceivingToAddress();

    }

    public String getFrom() { return this.from; }
    public String getTo() {return this.to;}
    public float getAmount() {return Float.parseFloat(this.amount);}
    public String getReceiveToAddress() {return this.address;}
    public String getPayingToAddress() {return this.address;}
}
