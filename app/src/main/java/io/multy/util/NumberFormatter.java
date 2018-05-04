/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberFormatter {

    public static DecimalFormat getInstance() {
        DecimalFormat cryptoFormatter = new DecimalFormat("##.########", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        cryptoFormatter.setMaximumFractionDigits(8);
        return cryptoFormatter;
    }

    public static DecimalFormat getFiatInstance() {
        DecimalFormat fiatFormatter = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        fiatFormatter.setMaximumFractionDigits(2);
        return fiatFormatter;
    }

    public static DecimalFormat getEthInstance() {
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(10);
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setGroupingUsed(false);
        return decimalFormat;
    }

}
