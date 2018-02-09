/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import io.realm.RealmObject;

public class RecentAddress extends RealmObject {

    private int currencyId;
    private String address;

    public RecentAddress() {
    }

    public RecentAddress(int currencyId, String address) {
        this.currencyId = currencyId;
        this.address = address;
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
