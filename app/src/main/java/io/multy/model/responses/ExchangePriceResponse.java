/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

public class ExchangePriceResponse {

    private Double USD;
    private Double EUR;
    private Double RUB;

    public Double getUSD() {
        return USD;
    }

    public Double getEUR() {
        return EUR;
    }

    public Double getRUB() {
        return RUB;
    }

    @Override
    public String toString() {
        return "ExchangePriceResponse{" +
                "USD='" + USD + '\'' +
                ", EUR='" + EUR + '\'' +
                ", RUB='" + RUB + '\'' +
                '}';
    }
}
