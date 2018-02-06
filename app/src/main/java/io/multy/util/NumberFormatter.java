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

    private static DecimalFormat cryptoFormatter;
    private static DecimalFormat fiatFormatter;

    public static DecimalFormat getInstance(){
        if (cryptoFormatter == null) {
            cryptoFormatter = new DecimalFormat("##.########", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            cryptoFormatter.setMaximumFractionDigits(8);
            return cryptoFormatter;
        } else {
            return cryptoFormatter;
        }
    }

    public static DecimalFormat getFiatInstance(){
        if (fiatFormatter == null) {
            fiatFormatter = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            fiatFormatter.setMaximumFractionDigits(2);
            return fiatFormatter;
        } else {
            return fiatFormatter;
        }
    }

}
