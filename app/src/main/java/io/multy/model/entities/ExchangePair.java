/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

public class ExchangePair {
    private String fromAsset;
    private String toAsset;
    private double rate;
    private String payingFromAddress;
    private String receivingToAddress;

    public ExchangePair(String from, String to, double rate){
        this.fromAsset = from;
        this.toAsset  = to;
        this.rate = rate;
    }

    public void setPayingFromAddress(String address) {this.payingFromAddress = address;}
    public void setReceivingToAddress(String address) {this.receivingToAddress = address;}
    public void setFromAsset(String from) {this.fromAsset = from;}
    public void setToAsset(String to) {this.toAsset = to;}
    public void setRate(float rate) {this.rate = rate;}

    public String getFromAsset() {return this.fromAsset;}
    public String getToAsset() {return this.toAsset;}
    public double getRate() {return this.rate;}
    public String getPayingFromAddress() {return this.payingFromAddress;}
    public String getReceivingToAddress() {return this.receivingToAddress;}

}
