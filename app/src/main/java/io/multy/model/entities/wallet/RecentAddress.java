/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RecentAddress extends RealmObject {

    public static final String CURRENCY_ID = "currencyId";
    public static final String ADDRESS = "address";
    public static final String NETWORK_ID = "networkId";
    public static final String RECENT_ADDRESS_ID = "recentAddressId";

    private int currencyId;
    private String address;
    private int networkId;
    @PrimaryKey
    private long recentAddressId;

    public RecentAddress() {
    }

    public RecentAddress(int currencyId, int networkId, String address) {
        this.currencyId = currencyId;
        this.address = address;
        this.networkId = networkId;
    }

    public RecentAddress(int currencyId, int networkId, String address,  long recentAddressId) {
        this.currencyId = currencyId;
        this.address = address;
        this.networkId = networkId;
        this.recentAddressId = recentAddressId;
    }

    public long getRecentAddressId() {
        return recentAddressId;
    }

    public void setRecentAddressId(long recentAddressId) {
        this.recentAddressId = recentAddressId;
    }

    public int getNetworkId() {
        return networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static long stringToId(String inputString) {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < inputString.length() / 6; i++) {
            id.append(String.valueOf(inputString.codePointAt(i)));
        }
        return Long.parseLong(id.toString());
    }

}
