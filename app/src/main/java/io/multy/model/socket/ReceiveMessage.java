/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.socket;

import com.google.gson.annotations.SerializedName;

public class ReceiveMessage {
    @SerializedName("type")
    private int type;
    @SerializedName("from")
    private String from;
    @SerializedName("to")
    private String to;
    @SerializedName("date")
    private long date;
    @SerializedName("status")
    private int status;
    @SerializedName("payload")
    private Payload payload;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public long getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public class Payload {

        @SerializedName("Hash")
        private String hash;
        @SerializedName("From")
        private String from;
        @SerializedName("To")
        private String to;
        @SerializedName("Amount")
        private String amount;
        @SerializedName("input")
        private String input;
        @SerializedName("GasPrice")
        private String gasPrice;
        @SerializedName("GasLimit")
        private String gasLimit;
        @SerializedName("Nonce")
        private int nonce;
        @SerializedName("Status")
        private int status;
        @SerializedName("TxpoolTime")
        private long txpoolTime;
        @SerializedName("BlockTime")
        private long blockTime;
        @SerializedName("BlockHeight")
        private long blockHeight;
        @SerializedName("Multisig")
        private boolean multisig;
        @SerializedName("InvocationStatus")
        private boolean invocationStatus;

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getGasPrice() {
            return gasPrice;
        }

        public void setGasPrice(String gasPrice) {
            this.gasPrice = gasPrice;
        }

        public String getGasLimit() {
            return gasLimit;
        }

        public void setGasLimit(String gasLimit) {
            this.gasLimit = gasLimit;
        }

        public int getNonce() {
            return nonce;
        }

        public void setNonce(int nonce) {
            this.nonce = nonce;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public long getTxpoolTime() {
            return txpoolTime;
        }

        public void setTxpoolTime(int txpoolTime) {
            this.txpoolTime = txpoolTime;
        }

        public boolean isMultisig() {
            return multisig;
        }

        public void setMultisig(boolean multisig) {
            this.multisig = multisig;
        }

        public long getBlockTime() {
            return blockTime;
        }

        public void setBlockTime(long blockTime) {
            this.blockTime = blockTime;
        }

        public long getBlockHeight() {
            return blockHeight;
        }

        public void setBlockHeight(long blockHeight) {
            this.blockHeight = blockHeight;
        }

        public boolean isInvocationStatus() {
            return invocationStatus;
        }

        public void setInvocationStatus(boolean invocationStatus) {
            this.invocationStatus = invocationStatus;
        }
    }
}
