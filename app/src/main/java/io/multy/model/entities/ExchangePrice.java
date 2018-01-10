/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ExchangePrice extends RealmObject {

    @PrimaryKey
    private int id = 0;
    private Double exchangePrice;

    public ExchangePrice() {
    }

    public ExchangePrice(Double exchangePrice) {
        this.exchangePrice = exchangePrice;
    }

    public Double getExchangePrice() {
        return exchangePrice;
    }

    public void setExchangePrice(Double exchangePrice) {
        this.exchangePrice = exchangePrice;
    }
}
