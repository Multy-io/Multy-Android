/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api.socket;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CurrenciesRate extends RealmObject {

    @PrimaryKey
    private int id = 0;
    @SerializedName("btc_usd")
    private double btcToUsd;
    @SerializedName("BTCtoEUR")
    private double btcToEur;

    @SerializedName("ETHtoUSD")
    private double ethToUsd;
    @SerializedName("ETHtoEUR")
    private double ethToEur;

    public double getBtcToUsd() {
        return btcToUsd;
    }

    public void setBtcToUsd(double btcToUsd) {
        this.btcToUsd = btcToUsd;
    }

    public double getBtcToEur() {
        return btcToEur;
    }

    public void setBtcToEur(double btcToEur) {
        this.btcToEur = btcToEur;
    }

    public double getEthToUsd() {
        return ethToUsd;
    }

    public void setEthToUsd(double ethToUsd) {
        this.ethToUsd = ethToUsd;
    }

    public double getEthToEur() {
        return ethToEur;
    }

    public void setEthToEur(double ethToEur) {
        this.ethToEur = ethToEur;
    }
}
