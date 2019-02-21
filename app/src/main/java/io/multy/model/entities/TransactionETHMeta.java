/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import io.multy.model.entities.wallet.Wallet;

public class TransactionETHMeta {
    private Wallet wallet;
    private String amount;
    private String feeRate;
    private String gasLimit;
    private String toAddress;


    private String donationAmount;
    private String donationAddress;

    private boolean payingForComission;

    private String meta;

    public TransactionETHMeta(Wallet wallet, String feeRate, String gasLimit, String toAddress){
        this.wallet = wallet;
        this.feeRate = feeRate;
        this.gasLimit = gasLimit;
        this.toAddress = toAddress;
        this.payingForComission = true;
    }

    public Wallet getWallet() {return this.wallet;}
    public String getAmount() {return this.amount;}
    public String getFeeRate() {return this.feeRate;}
    public String getGasLimit() {return this.gasLimit;}

    public String getToAddress() {return this.toAddress;}

    public boolean isPayingForComission() { return payingForComission; }

    public String getMeta() {return this.meta;}

    public void setAmount(String amount) {this.amount = amount;}
    public void setFeeRate(String rate) {this.feeRate = rate;}

    public void setDonationAmount(String amount) {this.donationAmount = amount;}

    public void setMeta(String meta) {this.meta = meta;}

    public void setPayingForComission(boolean value) {this.payingForComission = value;}

}
