/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.ArrayList;

import io.multy.model.entities.Erc20TokenPrice;
import io.multy.util.TokenPriceTypeAdapter;

public class EthplorerResponse {

    @SerializedName("address")
    private String address;
    @SerializedName("countTxs")
    private int txCount;
    @SerializedName("ETH")
    private ETH eth;
    @SerializedName("tokens")
    private ArrayList<PlorerToken> tokens;

    public String getAddress() {
        return address;
    }

    public int getTxCount() {
        return txCount;
    }

    public ETH getEth() {
        return eth;
    }

    public ArrayList<PlorerToken> getTokens() {
        return tokens;
    }

    public class ETH {
        @SerializedName("balance")
        public String balance;
    }

    public class PlorerToken {
        @SerializedName("tokenInfo")
        private PlorerTokenInfo tokenInfo;
        @SerializedName("balance")
        private BigDecimal balance;

        public PlorerToken(PlorerTokenInfo tokenInfo, BigDecimal balance) {
            this.tokenInfo = tokenInfo;
            this.balance = balance;
        }

        public PlorerTokenInfo getTokenInfo() {
            return tokenInfo;
        }

        public BigDecimal getBalance() {
            return balance;
        }
    }

    public class PlorerTokenInfo {
        @SerializedName("address")
        private String contractAddress;
        @SerializedName("name")
        private String name;
        @SerializedName("decimals")
        private int decimals;
        @SerializedName("symbol")
        private String symbol;
        @SerializedName("totalSupply")
        private String totalSupply;
        @SerializedName("owner")
        private String ownerAddress;
        @SerializedName("lastUpdated")
        private long lastUpdated;
        @SerializedName("issuancesCount")
        private int issuancesCount;
        @SerializedName("holdersCount")
        private int holdersCount;
        @SerializedName("description")
        private String description;
        @SerializedName("ethTransfersCount")
        private int ethTransferCount;
        @SerializedName("price")
        @JsonAdapter(TokenPriceTypeAdapter.class)
        private Erc20TokenPrice price;

        public PlorerTokenInfo(String contractAddress, String name, int decimals, String symbol, String totalSupply, String ownerAddress, long lastUpdated, int issuancesCount, int holdersCount, String description, int ethTransferCount, Erc20TokenPrice price) {
            this.contractAddress = contractAddress;
            this.name = name;
            this.decimals = decimals;
            this.symbol = symbol;
            this.totalSupply = totalSupply;
            this.ownerAddress = ownerAddress;
            this.lastUpdated = lastUpdated;
            this.issuancesCount = issuancesCount;
            this.holdersCount = holdersCount;
            this.description = description;
            this.ethTransferCount = ethTransferCount;
            this.price = price;
        }

        public String getContractAddress() {
            return contractAddress;
        }

        public String getName() {
            return name;
        }

        public int getDecimals() {
            return decimals;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getTotalSupply() {
            return totalSupply;
        }

        public String getOwnerAddress() {
            return ownerAddress;
        }

        public long getLastUpdated() {
            return lastUpdated;
        }

        public int getIssuancesCount() {
            return issuancesCount;
        }

        public int getHoldersCount() {
            return holdersCount;
        }

        public String getDescription() {
            return description;
        }

        public int getEthTransferCount() {
            return ethTransferCount;
        }

        public Erc20TokenPrice getPrice() {
            return price;
        }
    }
}
