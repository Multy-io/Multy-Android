/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.os.Build;

import io.multy.Multy;
import io.multy.R;

public final class Constants {

    public static final String DEVICE_NAME = "Andrdoid " + Build.MANUFACTURER
            + " " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")"
            + " " + Build.VERSION.RELEASE
            + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
    public static final int GAS_LIMIT_VALUE = 21000;

    public static final String BASE_URL = Multy.getContext().getString(R.string.base_url);

    public static final int DONATION_MIN_VALUE = 10000;
    public static final String DONATION_ADDRESS_TESTNET = "mnUtMQcs3s8kSkSRXpREVtJamgUCWpcFj4";

    public static final int ANDROID_OS_ID = 1;

    public static final int POSITION_ASSETS = 0;
    public static final int POSITION_FEED = 1;
    public static final int POSITION_CONTACTS = 3;
    public static final int POSITION_SETTINGS = 4;

    public static final String SPAN_DIVIDER = ",";
    public static final String SPACE = " ";
    public static final String NEW_LINE = "\n";
    public static final String QUESTION_MARK = "?";
    public static final String EQUAL = "=";

    public static final String PREF_APP_INITIALIZED = "is_first_start";
    public static final String PREF_BACKUP_SEED = "backup_seed";
    public static final String PREF_AUTH = "pref_auth";
    public static final String PREF_EXCHANGE_PRICE = "pref_exchange_price";
    public static final String PREF_WALLET_TOP_INDEX_BTC = "pref_wallet_top_index_btc";
    public static final String PREF_WALLET_TOP_INDEX_ETH = "pref_wallet_top_index_eth";
    public static final String PREF_IS_FINGERPRINT_ENABLED = "PREF_IS_FINGERPRINT_ENABLED";
    public static final String PIN_COUNTER = "PIN_COUNTER";
    public static final String ANDROID_PACKAGE = "android.webkit.";
    public static final String PREF_PIN = "PREF_PIN";
    public static final String PREF_IV = "PREF_IV";
    public static final String PREF_KEY = "PREF_KEY";
    public static final String PREF_LOCK = "PREF_LOCK";
    public static final String PREF_UNLOCKED = "PREF_UNLOCKED";
    public static final String PREF_LOCK_DATE = "PREF_LOCK_DATE";
    public static final String PREF_LOCK_MULTIPLIER = "PREF_LOCK_MULTIPLIER";
    public static final String PREF_VERSION = "PREF_VERSION";
    public static final String PREF_SELF_CLICKED = "PREF_SELF_CLICKED";
    public static final String PREF_DONATE_ADDRESS_BTC = "PREF_DONATE_ADDRESS_BTC";
    public static final String PREF_DONATE_ADDRESS_ETH = "PREF_DONATE_ADDRESS_ETH";
    public static final String FLAG_VIEW_SEED_PHRASE = "view_seed_phrase";
    public static final String PREF_IS_PUSH_ENABLED = "PREF_IS_PUSH_ENABLED";
    public static final String PREF_TERMS_ACCEPTED = "TERMS_TERMS_ACCEPTED";

    public static final int REQUEST_CODE_SET_CHAIN = 560;
    public static final int REQUEST_CODE_SET_GAS = 560;
    public static final String CHAIN_NAME = "chain_name";
    public static final String CHAIN_NET = "chain_net";
    public static final String GAS_PRICE = "gas_price";
    public static final String GAS_LIMIT = "gas_limit";

    public static final int CAMERA_REQUEST_CODE = 328;
    public static final String EXTRA_QR_CONTENTS = "EXTRA_QR_CONTENTS";
    public static final String EXTRA_WALLET_ID = "EXTRA_WALLET_ID";
    public static final String EXTRA_SENDER_ADDRESS = "EXTRA_SENDER_ADDRESS";
    public static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";
    public static final String EXTRA_AMOUNT = "EXTRA_AMOUNT";
    public static final String EXTRA_RESTORE = "EXTRA_RESTORE";
    public static final String EXTRA_DONATION_CODE = "EXTRA_DONATION_CODE";

    public static final String DEEP_LINK_QR_CODE = "QR_CODE";

    public final static String BTC = "BTC";
    public final static String ETH = "ETH";
    public final static String USD = "USD";
    public final static String EUR = "EUR";

    public static final int TX_MEMPOOL_INCOMING = 1;
    public static final int TX_MEMPOOL_OUTCOMING = 3;
    public static final int TX_IN_BLOCK_INCOMING = 2;
    public static final int TX_IN_BLOCK_OUTCOMING = 4;
    public static final int TX_CONFIRMED_INCOMING = 5;
    public static final int TX_CONFIRMED_OUTCOMING = 6;

    public static final int REQUEST_CODE_RESTORE = 22;
    public static final int REQUEST_CODE_CREATE = 22;

    public static final String BLOCKCHAIN_TEST_INFO_PATH = "https://testnet.blockchain.info/tx/";
    public static final String BLOCKCHAIN_MAIN_INFO_PATH = "https://blockchain.info/tx/";
    public static final String ETHERSCAN_RINKEBY_INFO_PATH = "https://rinkeby.etherscan.io/tx/";
    public static final String ETHERSCAN_MAIN_INFO_PATH = "https://etherscan.io/tx/";
    public static final String PUSH_TOPIC = "TransactionUpdate-";
    public static final String MULTY_IO_URL = "http://multy.io";
    public static final String CHAIN_ID = "CHAIN_ID";
    public static final String FEATURE_ID = "FEATURE_ID";

    public static final int DONATE_WITH_TRANSACTION = 10000;
    public static final int DONATE_ADDING_ACTIVITY = 10200;
    public static final int DONATE_ADDING_CONTACTS = 10201;
    public static final int DONATE_ADDING_PORTFOLIO = 10202;
    public static final int DONATE_ADDING_CHARTS = 10203;
    public static final int DONATE_ADDING_IMPORT_WALLET = 10300;
    public static final int DONATE_ADDING_EXCHANGE = 10301;
    public static final int DONATE_ADDING_WIRELESS_SCAN = 10302;

    public static final int MIN_SATOSHI = 546;
    public static final int BTC_TRANSACTION_SIZE = 225;

    public static final String[] rootFiles = {
            "init.magisk.rc",
            "magisk",
            "Superuser.apk",
            "daemonsu",
            "su",
            "frida",
            "frida-server",
            "fridaserver",
            "magisk.log",
            "last_magisk.log",
            "superuser"
    };

    public static final String[] rootApplications = {
            "com.saurik.substrate",
            "com.topjohnwu.magisk",
            "com.android.vending.billing.InAppBillingService.COIN",
            "com.chelpus.luckypatcher",
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.zachspong.temprootremovejb",
            "com.ramdroid.appquarantine"
    };

    public static final String[] rootPaths = {
            "/data/",
            "/data/data",
            "/data/local/",
            "/data/local/tmp/",
            "/cache/",
            "/",
            "/system/xbin/",
            "/system/",
            "/dev/",
            "/etc/init.d/",
            "/system/bin/",
            "/sbin/",
            "/system/bin/.ext/"
    };
}
