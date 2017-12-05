/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;


import io.realm.RealmObject;

public class WalletAddress extends RealmObject {

    private int index;
    private String address;

    public WalletAddress() {
    }

    public WalletAddress(int index, String address) {
        this.index = index;
        this.address = address;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "WalletAddress{" +
                "index=" + index +
                ", address='" + address + '\'' +
                '}';
    }
}
