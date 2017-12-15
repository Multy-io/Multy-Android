/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import java.text.DecimalFormat;

public class CryptoFormatUtils {

    public static String satoshiToBtc(double satoshi) {
        return new DecimalFormat("#.##################").format(satoshi / Math.pow(10, 8));
    }
}
