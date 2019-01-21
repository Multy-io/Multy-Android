/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import io.multy.model.entities.wallet.Wallet;

public class TransactionBTCMeta {
    private Wallet wallet;
    private String amount;
    private String feeRate;

    private String toAddress;
    private String changeAddress;

    private String donationAmount;
    private String donationAddress;

    private boolean payingForComission;

    private String meta;

    public TransactionBTCMeta(Wallet wallet, String feeRate, String toAddress, String changeAddress){
        this.wallet = wallet;
        this.feeRate = feeRate;
        this.toAddress = toAddress;
        this.changeAddress = changeAddress;
        this.payingForComission = true;
    }

    public Wallet getWallet() {return this.wallet;}
    public String getAmount() {return this.amount;}
    public String getFeeRate() {return this.feeRate;}

    public String getToAddress() {return this.toAddress;}
    public String getChangeAddress() {return this.changeAddress;}

    public String getDonationAmount() {return this.donationAmount;}
    public String getDonationAddress() {return this.donationAddress;}

    public String getMeta() {return this.meta;}

    public boolean isPayingForComission() { return payingForComission; }

    public void setAmount(String amount) {this.amount = amount;}
    public void setFeeRate(String rate) {this.feeRate = rate;}

    public void setDonationAmount(String amount) {this.donationAmount = amount;}
    public void setDonationAddress(String address) {this.donationAddress = address;}

    public void setMeta(String meta) {this.meta = meta;}

    public void setPayingForComission(boolean value) {this.payingForComission = value;}

}
