/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberFormatter {

    private static DecimalFormat formatter;

    public static DecimalFormat getInstance(){
        if (formatter == null) {
            DecimalFormat formatter = new DecimalFormat("##.########", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            formatter.setMaximumFractionDigits(6);
            return formatter;
        } else {
            return formatter;
        }
    }
}
