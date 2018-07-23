/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.requests;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anschutz1927@gmail.com on 23.07.18.
 */
public class CreateMultisigRequest {

    @SerializedName("currencyID")
    private int currencyId;
    @SerializedName("networkID")
    private int networkId;
    @SerializedName("address")
    private String address;
    @SerializedName("addressIndex")
    private int addressIndex;
    @SerializedName("walletIndex")
    private int walletIndex;
    @SerializedName("walletName")
    private String walletName;
    @SerializedName("multisig")
    private Multisig multisig;

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }

    public int getNetworkId() {
        return networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
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

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public Multisig getMultisig() {
        return multisig;
    }

    public void setMultisig(Multisig multisig) {
        this.multisig = multisig;
    }

    public static class Multisig {

        @SerializedName("isMultisig")
        private boolean isMultisig = true;
        @SerializedName("signaturesRequired")
        private int signatureRequired;
        @SerializedName("ownersCount")
        private int ownersCount;
        @SerializedName("inviteCode")
        private String inviteCode;

        public Multisig(int confirmsCount, int membersCount, String inviteCode) {
            signatureRequired = confirmsCount;
            ownersCount = membersCount;
            this.inviteCode = inviteCode;
        }

        public boolean isMultisig() {
            return isMultisig;
        }

        public void setMultisig(boolean multisig) {
            isMultisig = multisig;
        }

        public int getSignatureRequired() {
            return signatureRequired;
        }

        public void setSignatureRequired(int signatureRequired) {
            this.signatureRequired = signatureRequired;
        }

        public int getOwnersCount() {
            return ownersCount;
        }

        public void setOwnersCount(int ownersCount) {
            this.ownersCount = ownersCount;
        }

        public String getInviteCode() {
            return inviteCode;
        }

        public void setInviteCode(String inviteCode) {
            this.inviteCode = inviteCode;
        }
    }
}
