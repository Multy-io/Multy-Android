/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import java.text.DecimalFormat;

import io.multy.storage.RealmManager;

public class CryptoFormatUtils {

    private final static DecimalFormat formatBtc = new DecimalFormat("#.##################");
    private final static DecimalFormat formatUsd = new DecimalFormat("#.##");

    public static String satoshiToBtc(double satoshi) {
        return formatBtc.format(satoshi / Math.pow(10, 8));
    }

    public static String satoshiToUsd(double satoshi) {
        double btc = satoshi / Math.pow(10, 8);
        double fiat = RealmManager.getSettingsDao().getCurrenciesRate().getBtcToUsd() * btc;
        return formatUsd.format(fiat);
    }

    /**
     * @param satoshi
     * @param price   usd per btc
     * @return amount in usd
     */
    public static String satoshiToUsd(double satoshi, double price) {
        double btc = (satoshi / Math.pow(10, 8));
        return formatUsd.format(btc * price);
    }
}
