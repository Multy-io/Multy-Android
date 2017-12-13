/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.util;

/**
 * Created by Ihar Paliashchuk on 09.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class Constants {

    public static final int POSITION_ASSETS = 0;
    public static final int POSITION_FEED = 1;
    public static final int POSITION_CONTACTS = 3;
    public static final int POSITION_SETTINGS = 4;

    public static final String SPAN_DIVIDER = ",";
    public static final String SPACE = " ";
    public static final String QUESTION_MARK = "?";

    public static final String PREF_SPAN = "span";
    public static final String PREF_FIRST_SUCCESSFUL_START = "first_start";
    public static final String PREF_AUTH = "pref_auth";
    public static final String PREF_EXCHANGE_PRICE = "pref_exchange_price";

    public static final int CAMERA_REQUEST_CODE = 328;
    public static final String EXTRA_QR_CONTENTS = "EXTRA_QR_CONTENTS";
    public static final String EXTRA_WALLET_ID = "EXTRA_WALLET_ID";

    public static final String DEEP_LINK_QR_CODE = "QR_CODE";

    public final static String BTC = "BTC";
    public final static String ETH = "ETH";
    public final static String USD = "USD";
    public final static String EUR = "EUR";

    public static final String ERROR_LOAD_EXCHANGE_PRICE = "Can't load exchange price. Will be used the last one";
    public static final String ERROR_ADDING_ADDRESS = "An error occurred while adding new address";
}
