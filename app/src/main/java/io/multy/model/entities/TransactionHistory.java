/*
 * Copyright 2018 Idealnaya rabota LLC
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
    @SerializedName("addresses")
    ArrayList<String> addresses;
    @SerializedName("txstatus")
    private int txStatus;
    @SerializedName("txoutamount")
    private long txOutAmount;
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
    @SerializedName("confirmations")
    private int confirmations;
    @SerializedName("mempooltime")
    private long mempoolTime;
    @SerializedName("from")
    private String from;
    @SerializedName("to")
    private String to;

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

    public ArrayList<String> getAddresses() {
        return addresses;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public int getTxStatus() {
        return txStatus;
    }

    public long getTxOutAmount() {
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

    public long getMempoolTime() {
        return mempoolTime;
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

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public class StockExchangeRate {
        @SerializedName("timestamp")
        private long timeStamp;

        @SerializedName("exchanges")
        private Exchanges exchanges;

        @SerializedName("stock_exchange")
        private String name;

        public long getTimeStamp() {
            return timeStamp;
        }

        public String getName() {
            return name;
        }

        public Exchanges getExchanges() {
            return exchanges;
        }
    }

    public class Exchanges {
        @SerializedName("btc_usd")
        private double btcUsd;
        @SerializedName("eth_usd")
        private double ethUsd;

        public double getBtcUsd() {
            return btcUsd;
        }

        public double getEthUsd() {
            return ethUsd;
        }

    }
}
