/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import java.text.DecimalFormat;

import io.multy.api.socket.CurrenciesRate;
import io.multy.storage.RealmManager;

public class CryptoFormatUtils {

    private final static DecimalFormat formatBtc = new DecimalFormat("#.##################");
    private final static DecimalFormat formatUsd = new DecimalFormat("#.##");

    public static String satoshiToBtc(double satoshi) {
        if (satoshi == 0) {
            return "0.0";
        }
        String result = formatBtc.format(satoshi / Math.pow(10, 8));
        if (!result.equals("") && result.contains(",")) {
            result = result.replaceAll(",", ".");
        }
        return result;
    }

    public static String satoshiToUsd(double satoshi) {
        if (satoshi == 0) {
            return "0.0";
        }

        final CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        if (currenciesRate == null) {
            return "";
        }

        double btc = satoshi / Math.pow(10, 8);
        double fiat = currenciesRate.getBtcToUsd() * btc;

        String result = formatUsd.format(fiat);
        if (!result.equals("") && result.contains(",")) {
            result = result.replaceAll(",", ".");
        }
        return result;
    }

    /**
     * @param satoshi
     * @param price   usd per btc
     * @return amount in usd
     */
    public static String satoshiToUsd(double satoshi, double price) {
        if (satoshi == 0) {
            return "0.0";
        }

        final CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        if (currenciesRate == null) {
            return "";
        }

        double btc = (satoshi / Math.pow(10, 8));
        return formatUsd.format(btc * price);
    }
}
