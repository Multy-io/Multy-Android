/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import io.realm.RealmObject;

public class RecentAddress extends RealmObject {

    public static final String CURRENCY_ID = "currencyId";
    public static final String ADDRESS = "address";
    public static final String NETWORK_ID = "networkId";

    private int currencyId;
    private String address;
    private int networkId;

    public RecentAddress() {
    }

    public RecentAddress(int currencyId, int networkId, String address) {
        this.currencyId = currencyId;
        this.address = address;
        this.networkId = networkId;
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
}
