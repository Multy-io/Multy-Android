/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import com.google.gson.annotations.SerializedName;

//TODO builder.pattern please
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
    public Payload payload;

    public MultisigEvent(int type, String from, String to, long date, int status, Payload payload) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.date = date;
        this.status = status;
        this.payload = payload;
    }

    public MultisigEvent(int type, long date, Payload payload) {
        this.type = type;
        this.date = date;
        this.payload = payload;
    }

    public static class Payload {

        @SerializedName("userid")
        public String userId;
        @SerializedName("address")
        public String address;
        @SerializedName("invitecode")
        public String inviteCode;
        @SerializedName("addresstokick")
        public String addressToKick;
        @SerializedName("walletindex")
        public int walletIndex;
        @SerializedName("currencyid")
        public int currencyId;
        @SerializedName("networkid")
        public int networkId;
        @SerializedName("exists")
        public boolean exist;
        @SerializedName("txid")
        public String txId;

        //TODO builder.pattern please
        public Payload() {
        }

        public Payload(String userId, String address, String inviteCode, String addressToKick, int walletIndex, int currencyId, int networkId) {
            this.userId = userId;
            this.address = address;
            this.inviteCode = inviteCode;
            this.addressToKick = addressToKick;
            this.walletIndex = walletIndex;
            this.currencyId = currencyId;
            this.networkId = networkId;
        }
    }
}
