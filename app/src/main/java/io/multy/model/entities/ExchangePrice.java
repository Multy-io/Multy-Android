/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;


import io.realm.RealmObject;

public class ExchangePrice extends RealmObject {

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

    @Override
    public String toString() {
        return "ExchangePrice{" +
                "exchangePrice=" + exchangePrice +
                '}';
    }
}
