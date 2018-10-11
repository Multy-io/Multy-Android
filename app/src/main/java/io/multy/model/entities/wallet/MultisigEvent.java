/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import com.google.gson.annotations.SerializedName;

public class MultisigEvent {

    @SerializedName("type")
    private int type;
    @SerializedName("from")
    private String from = "";
    @SerializedName("to")
    private String to = "";
    @SerializedName("date")
    private long date; //unix
    @SerializedName("status")
    private int status;
    @SerializedName("payload")
    private Payload payload;

    private MultisigEvent() { }

    public int getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public long getDate() {
        return date;
    }

    public int getStatus() {
        return status;
    }

    public Payload getPayload() {
        return payload;
    }

    public static MultisigEventBuilder getBuilder() {
        return new MultisigEvent().new MultisigEventBuilder();
    }

    public class MultisigEventBuilder {

        private MultisigEventBuilder() {}

        public MultisigEvent build() {
            return MultisigEvent.this;
        }

        public MultisigEventBuilder setType(int type) {
            MultisigEvent.this.type = type;
            return this;
        }

        public MultisigEventBuilder setFrom(String from) {
            MultisigEvent.this.from = from;
            return this;
        }

        public MultisigEventBuilder setTo(String to) {
            MultisigEvent.this.to = to;
            return this;
        }

        public MultisigEventBuilder setDate(long date) {
            MultisigEvent.this.date = date;
            return this;
        }

        public MultisigEventBuilder setStatus(int status) {
            MultisigEvent.this.status = status;
            return this;
        }

        public MultisigEventBuilder setPayload(Payload payload) {
            MultisigEvent.this.payload = payload;
            return this;
        }
    }

    public static class Payload {

        @SerializedName("userid")
        private String userId;
        @SerializedName("address")
        private String address;
        @SerializedName("invitecode")
        private String inviteCode;
        @SerializedName("addresstokick")
        private String addressToKick;
        @SerializedName("walletindex")
        private int walletIndex;
        @SerializedName("currencyid")
        private int currencyId;
        @SerializedName("networkid")
        private int networkId;
        @SerializedName("exists")
        private boolean exist;
        @SerializedName("txid")
        private String txId;

        public Payload() { }

        public String getUserId() {
            return userId;
        }

        public String getAddress() {
            return address;
        }

        public String getInviteCode() {
            return inviteCode;
        }

        public String getAddressToKick() {
            return addressToKick;
        }

        public int getWalletIndex() {
            return walletIndex;
        }

        public int getCurrencyId() {
            return currencyId;
        }

        public int getNetworkId() {
            return networkId;
        }

        public boolean isExist() {
            return exist;
        }

        public String getTxId() {
            return txId;
        }

        public static PayloadBuilder getBuilder() {
            return new Payload().new PayloadBuilder();
        }

        public class PayloadBuilder {

            private PayloadBuilder() {}

            public Payload build() {
                return Payload.this;
            }

            public PayloadBuilder setUserId(String userId) {
                Payload.this.userId = userId;
                return this;
            }

            public PayloadBuilder setAddress(String address) {
                Payload.this.address = address;
                return this;
            }

            public PayloadBuilder setInviteCode(String inviteCode) {
                Payload.this.inviteCode = inviteCode;
                return this;
            }

            public PayloadBuilder setAddressToKick(String addressToKick) {
                Payload.this.addressToKick = addressToKick;
                return this;
            }

            public PayloadBuilder setWalletIndex(int walletIndex) {
                Payload.this.walletIndex = walletIndex;
                return this;
            }

            public PayloadBuilder setCurrencyId(int currencyId) {
                Payload.this.currencyId = currencyId;
                return this;
            }

            public PayloadBuilder setNetworkId(int networkId) {
                Payload.this.networkId = networkId;
                return this;
            }

            public PayloadBuilder setExist(boolean exist) {
                Payload.this.exist = exist;
                return this;
            }

            public PayloadBuilder setTxId(String txId) {
                Payload.this.txId = txId;
                return this;
            }
        }

//        public Payload(String userId, String address, String inviteCode, String addressToKick, int walletIndex, int currencyId, int networkId) {
//            this.userId = userId;
//            this.address = address;
//            this.inviteCode = inviteCode;
//            this.addressToKick = addressToKick;
//            this.walletIndex = walletIndex;
//            this.currencyId = currencyId;
//            this.networkId = networkId;
//        }
    }


//    public MultisigEvent(int type, String from, String to, long date, int status, Payload payload) {
//        this.type = type;
//        this.from = from;
//        this.to = to;
//        this.date = date;
//        this.status = status;
//        this.payload = payload;
//    }
//
//    public MultisigEvent(int type, long date, Payload payload) {
//        this.type = type;
//        this.date = date;
//        this.payload = payload;
//    }
}
