/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import io.multy.model.entities.wallet.WalletAddress;

public class TransactionHistory {

    @SerializedName("txid")
    private String txId;
    @SerializedName("txhash")
    private String txHash;
    @SerializedName("txoutscript")
    private String txOutScript;
    @SerializedName("address")
    private String address;
    @SerializedName("txstatus")
    private int txStatus;
    @SerializedName("txoutamount")
    private double txOutAmount;
    @SerializedName("txoutid")
    private int txOutId;
    @SerializedName("walletindex")
    private int walletIndex;
    @SerializedName("blocktime")
    private long blockTime;
    @SerializedName("blockheight")
    private int blockHeight;
    @SerializedName("txfee")
    private long txFee;

    //exchange price while transaction happen
    @SerializedName("btctousd")
    private double btcToUsd;

    @SerializedName("stockexchangerate")
    ArrayList<StockExchangeRate> stockExchangeRates;
    @SerializedName("txinputs")
    ArrayList<WalletAddress> inputs;
    @SerializedName("txoutputs")
    ArrayList<WalletAddress> outputs;

    public String getTxId() {
        return txId;
    }

    public String getTxHash() {
        return txHash;
    }

    public String getTxOutScript() {
        return txOutScript;
    }

    public String getAddress() {
        return address;
    }

    public int getTxStatus() {
        return txStatus;
    }

    public double getTxOutAmount() {
        return txOutAmount;
    }

    public int getTxOutId() {
        return txOutId;
    }

    public int getWalletIndex() {
        return walletIndex;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public long getTxFee() {
        return txFee;
    }

    public double getBtcToUsd() {
        return btcToUsd;
    }

    public ArrayList<WalletAddress> getInputs() {
        return inputs;
    }

    public ArrayList<WalletAddress> getOutputs() {
        return outputs;
    }

    public ArrayList<StockExchangeRate> getStockExchangeRates() {
        return stockExchangeRates;
    }

    public class StockExchangeRate {
        @SerializedName("btc_usd")
        private double btcUsd;
        @SerializedName("eth_usd")
        private double ethUsd;

        @SerializedName("timestamp")
        private long timeStamp;

        @SerializedName("stock_exchange")
        private String name;

        public double getBtcUsd() {
            return btcUsd;
        }

        public double getEthUsd() {
            return ethUsd;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public String getName() {
            return name;
        }
    }
}
