/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.requests;

import com.google.gson.annotations.SerializedName;

public class WalletRequest {

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
    @SerializedName("isImported")
    private boolean isImported;
    @SerializedName("multisig")
    private Multisig multisig;

    private WalletRequest() { }

    public int getCurrencyId() {
        return currencyId;
    }

    public int getNetworkId() {
        return networkId;
    }

    public String getAddress() {
        return address;
    }

    public int getAddressIndex() {
        return addressIndex;
    }

    public int getWalletIndex() {
        return walletIndex;
    }

    public String getWalletName() {
        return walletName;
    }

    public Multisig getMultisig() {
        return multisig;
    }

    public boolean isImported() {
        return isImported;
    }

    public static Builder getBulilder() {
        return new WalletRequest().new Builder();
    }

    public class Builder {

        private Builder() { }

        public WalletRequest build() {
            WalletRequest instance = new WalletRequest();
            instance.currencyId = currencyId;
            instance.networkId = networkId;
            instance.address = address;
            instance.addressIndex = addressIndex;
            instance.walletIndex = walletIndex;
            instance.walletName = walletName;
            instance.isImported = isImported;
            instance.multisig = multisig;
            return instance;
        }

        public Builder setCurrencyId(int currencyId) {
            WalletRequest.this.currencyId = currencyId;
            return this;
        }

        public Builder setNetworkId(int networkId) {
            WalletRequest.this.networkId = networkId;
            return this;
        }

        public Builder setAddress(String address) {
            WalletRequest.this.address = address;
            return this;
        }

        public Builder setAddressIndex(int addressIndex) {
            WalletRequest.this.addressIndex = addressIndex;
            return this;
        }

        public Builder setWalletIndex(int walletIndex) {
            WalletRequest.this.walletIndex = walletIndex;
            return this;
        }

        public Builder setWalletName(String walletName) {
            WalletRequest.this.walletName = walletName;
            return this;
        }

        public Builder setImported(boolean isImported) {
            WalletRequest.this.isImported = isImported;
            return this;
        }

        public Builder setMultisig(Multisig multisig) {
            WalletRequest.this.multisig = multisig;
            return this;
        }
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
        @SerializedName("isImported")
        private boolean isImported = false;
        @SerializedName("contractAddress")
        private String multisigAddress;

        private Multisig() { }

        public boolean isMultisig() {
            return isMultisig;
        }

        public boolean isImported() {
            return isImported;
        }

        public int getSignatureRequired() {
            return signatureRequired;
        }

        public int getOwnersCount() {
            return ownersCount;
        }

        public String getInviteCode() {
            return inviteCode;
        }

        public String getMultisigAddress() {
            return multisigAddress;
        }

        public static Builder getBuilder() {
            return new Multisig().new Builder();
        }

        public class Builder {

            private Builder() { }

            public Multisig build() {
                Multisig instance = new Multisig();
                instance.isMultisig = isMultisig;
                instance.signatureRequired = signatureRequired;
                instance.ownersCount = ownersCount;
                instance.inviteCode = inviteCode;
                instance.isImported = isImported;
                instance.multisigAddress = multisigAddress;
                return instance;
            }

            public Builder setIsMultisig(boolean isMultisig) {
                Multisig.this.isMultisig = isMultisig;
                return this;
            }

            public Builder setSignatureRequired(int signatureRequired) {
                Multisig.this.signatureRequired = signatureRequired;
                return this;
            }

            public Builder setOwnerCount(int ownersCount) {
                Multisig.this.ownersCount = ownersCount;
                return this;
            }

            public Builder setInviteCode(String inviteCode) {
                Multisig.this.inviteCode = inviteCode;
                return this;
            }

            public Builder setIsImported(boolean isImported) {
                Multisig.this.isImported = isImported;
                return this;
            }

            public Builder setMultisigAddress(String multisigAddress) {
                Multisig.this.multisigAddress = multisigAddress;
                return this;
            }
        }
    }
}
