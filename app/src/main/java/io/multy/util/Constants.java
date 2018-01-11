/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.util;

public class Constants {

    public static final String DEVICE_NAME = "Andrdoid " + " " + android.os.Build.MANUFACTURER + " " + android.os.Build.PRODUCT;

    public static final int POSITION_ASSETS = 0;
    public static final int POSITION_FEED = 1;
    public static final int POSITION_CONTACTS = 3;
    public static final int POSITION_SETTINGS = 4;

    public static final String SPAN_DIVIDER = ",";
    public static final String SPACE = " ";
    public static final String QUESTION_MARK = "?";
    public static final String EQUAL = "=";

    public static final String PREF_APP_INITIALIZED = "is_first_start";
    public static final String PREF_BACKUP_SEED = "backup_seed";
    public static final String PREF_AUTH = "pref_auth";
    public static final String PREF_EXCHANGE_PRICE = "pref_exchange_price";
    public static final String PREF_LOCK ="pref_lock";
    public static final String PREF_WALLET_TOP_INDEX = "pref_wallet_top_index";
    public static final String PREF_LOCK = "pref_lock";
    public static final String PREF_IS_FINGERPRINT_ENABLED = "PREF_IS_FINGERPRINT_ENABLED";
    public static final String PIN_COUNTER = "PIN_COUNTER";
    public static final String ANDROID_PACKAGE = "android.webkit.";
    public static final String PREF_PIN = "PREF_PIN";
    public static final String PREF_IV = "PREF_IV";
    public static final String PREF_KEY = "PREF_KEY";
    public static final String PREF_LOCK_DATE = "PREF_LOCK_DATE";
    public static final String PREF_LOCK_MULTIPLIER = "PREF_LOCK_MULTIPLIER";
    public static final String PREF_VERSION = "PREF_VERSION";


    public static final int CAMERA_REQUEST_CODE = 328;
    public static final String EXTRA_QR_CONTENTS = "EXTRA_QR_CONTENTS";
    public static final String EXTRA_WALLET_ID = "EXTRA_WALLET_ID";
    public static final String EXTRA_SENDER_ADDRESS = "EXTRA_SENDER_ADDRESS";
    public static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";
    public static final String EXTRA_AMOUNT = "EXTRA_AMOUNT";
    public static final String EXTRA_RESTORE = "EXTRA_RESTORE";

    public static final String DEEP_LINK_QR_CODE = "QR_CODE";

    public final static String BTC = "BTC";
    public final static String ETH = "ETH";
    public final static String USD = "USD";
    public final static String EUR = "EUR";

    public static final String ERROR_LOAD_EXCHANGE_PRICE = "Can't load exchange price. Will be used the last one";
    public static final String ERROR_ADDING_ADDRESS = "An error occurred while adding new address";

    public static final int ZERO = 0;
    public static final int ONE = 1;

    public static final String NOTIFICATION_CHANNEL = "Multy wallet";

}
