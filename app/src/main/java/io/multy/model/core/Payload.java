/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.core;

import com.google.gson.annotations.SerializedName;

public class Payload {

    @SerializedName("balance")
    private String balance;
    @SerializedName("destination_address")
    private String destinationAddress;
    @SerializedName("destination_amount")
    private String destinationAmount;

    @SerializedName("balance_eth")
    private String balanceEth;
    @SerializedName("contract_address")
    private String contractAddress;
    @SerializedName("balance_token")
    private String tokenBalance;
    @SerializedName("transfer_amount_token")
    private String tokenTransferAmount;

    /**
     * CONSTRUCTOR FOR ERC20
     *
     * @param balanceEth          OF SENDER WALLET
     * @param contractAddress
     * @param tokenBalance
     * @param tokenTransferAmount
     * @param destinationAddress
     */
    public Payload(String balanceEth, String contractAddress, String tokenBalance, String tokenTransferAmount, String destinationAddress) {
        this.balanceEth = balanceEth;
        this.contractAddress = contractAddress;
        this.tokenBalance = tokenBalance;
        this.tokenTransferAmount = tokenTransferAmount;
        this.destinationAddress = destinationAddress;
    }

    /**
     * CONSTRUCTOR FOR BASIC ETH TRANSACTION
     *
     * @param balance            OF SENDER WALLET IN WEI
     * @param destinationAddress
     * @param destinationAmount
     */
    public Payload(String balance, String destinationAddress, String destinationAmount) {
        this.balance = balance;
        this.destinationAddress = destinationAddress;
        this.destinationAmount = destinationAmount;
    }

    public Payload() {
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public void setDestinationAmount(String destinationAmount) {
        this.destinationAmount = destinationAmount;
    }
}
