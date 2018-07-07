/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by anschutz1927@gmail.com on 21.06.18.
 */
public class ContactAddress extends RealmObject {

    public static final String ADDRESS = "address";
    public static final String CONTACT_ID = "contactId";

    @PrimaryKey
    private String address;
    private long contactId;
    private int currencyId;
    private int networkId;
    private int addressCurrencyImgId = 0;

    public ContactAddress() {}

    public ContactAddress(String address, long contactId, int currencyId, int networkId, int addressCurrencyImgId) {
        this.address = address;
        this.contactId = contactId;
        this.currencyId = currencyId;
        this.networkId = networkId;
        this.addressCurrencyImgId = addressCurrencyImgId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public int getCurrencyImgId() {
        return addressCurrencyImgId;
    }

    public void setCurrencyImgId(int addressCurrencyImgId) {
        this.addressCurrencyImgId = addressCurrencyImgId;
    }

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
}
