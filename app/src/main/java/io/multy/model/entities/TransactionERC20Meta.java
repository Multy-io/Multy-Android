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

import java.math.BigDecimal;
import java.math.BigInteger;

import io.multy.model.entities.wallet.Wallet;

public class TransactionERC20Meta {
    private Wallet wallet;
    private String amount;
    private String feeRate;
    private String gasLimit;
    private String toAddress;
    private String tokenBalance;
    private String contractAddress;

    private BigDecimal decimals;


    private boolean payingForComission;

    private String meta;

    public TransactionERC20Meta(Wallet wallet, String feeRate, String gasLimit, String toAddress, ERC20TokenDAO token){
        this.wallet = wallet;
        this.feeRate = feeRate;
        this.gasLimit = gasLimit;
        this.toAddress = toAddress;
        this.payingForComission = true;
        this.decimals = BigDecimal.TEN.pow(token.getDecimals());

        BigDecimal bigBalance = BigDecimal.valueOf(Double.parseDouble(token.getBalance()));

        this.tokenBalance = bigBalance.multiply(decimals).toBigInteger().toString();

//        this.tokenBalance = String.valueOf(Long.valueOf(decimals * Double.parseDouble(token.getBalance())));
        this.contractAddress = token.getContractAddress();

    }

    public Wallet getWallet() {return this.wallet;}
    public String getAmount() {return this.amount;}
    public String getFeeRate() {return this.feeRate;}
    public String getGasLimit() {return this.gasLimit;}
    public String getTokenBalance() {return this.tokenBalance;}
    public String getContractAddress(){return this.contractAddress;}

    public String getToAddress() {return this.toAddress;}

    public boolean isPayingForComission() { return payingForComission; }

    public String getMeta() {return this.meta;}

    public void setAmount(String amount) {
        this.amount = BigDecimal.valueOf(Double.parseDouble(amount)).multiply(this.decimals).toBigInteger().toString();

    }
    public void setFeeRate(String rate) {this.feeRate = rate;}

    public void setMeta(String meta) {this.meta = meta;}

    public void setPayingForComission(boolean value) {this.payingForComission = value;}
    public BigDecimal getDecimals() {return this.decimals;}
}
