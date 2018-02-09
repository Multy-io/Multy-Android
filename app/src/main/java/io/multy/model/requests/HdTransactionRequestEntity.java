/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.requests;

import com.google.gson.annotations.SerializedName;

public class HdTransactionRequestEntity {

    @SerializedName("currencyid")
    private int currencyid;
    @SerializedName("payload")
    private Payload payload;

    public HdTransactionRequestEntity(int currencyid, Payload payload) {
        this.currencyid = currencyid;
        this.payload = payload;
    }

    public static class Payload {
        @SerializedName("address")
        private String address;
        @SerializedName("addressindex")
        private int addressIndex;
        @SerializedName("walletindex")
        private int walletIndex;
        @SerializedName("transaction")
        private String transaction;
        @SerializedName("ishd")
        private boolean isHd = true;

        public Payload(String address, int addressIndex, int walletIndex, String transaction) {
            this.address = address;
            this.addressIndex = addressIndex;
            this.walletIndex = walletIndex;
            this.transaction = transaction;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getAddressIndex() {
            return addressIndex;
        }

        public void setAddressIndex(int addressIndex) {
            this.addressIndex = addressIndex;
        }

        public int getWalletIndex() {
            return walletIndex;
        }

        public void setWalletIndex(int walletIndex) {
            this.walletIndex = walletIndex;
        }

        public String getTransaction() {
            return transaction;
        }

        public void setTransaction(String transaction) {
            this.transaction = transaction;
        }

        public boolean isHd() {
            return isHd;
        }

        public void setHd(boolean hd) {
            isHd = hd;
        }
    }
}
