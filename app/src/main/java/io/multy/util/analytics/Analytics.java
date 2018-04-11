/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

public class Analytics {

    private FirebaseAnalytics analytics;
    private static Analytics instance;

    private Analytics(Context context) {
        analytics = FirebaseAnalytics.getInstance(context);
    }

    public static Analytics getInstance(Context context) {
        if (instance == null) {
            instance = new Analytics(context);
        }

        return instance;
    }

    public void logFirstLaunch() {
        analytics.logEvent(AnalyticsConstants.FIRST_LAUNCH, null);
    }

    public void logFirstLaunchCreateWallet() {
        logEvent(AnalyticsConstants.FIRST_LAUNCH_NAME, AnalyticsConstants.FIRST_LAUNCH_NAME, AnalyticsConstants.FIRST_LAUNCH_CREATE_WALLET);
    }

    public void logFirstLaunchRestoreSeed() {
        logEvent(AnalyticsConstants.FIRST_LAUNCH_NAME, AnalyticsConstants.FIRST_LAUNCH_NAME, AnalyticsConstants.FIRST_LAUNCH_RESTORE_WALLET);
    }


    public void logCreateWalletLaunch() {
        analytics.logEvent(AnalyticsConstants.CREATE_WALLET, null);
    }

    public void logCreateWallet() {
        logEvent(AnalyticsConstants.CREATE_WALLET_NAME, AnalyticsConstants.CREATE_WALLET_NAME, AnalyticsConstants.CREATE_WALLET_CREATE);
    }

    public void logCreateWalletChain() {
        logEvent(AnalyticsConstants.CREATE_WALLET_NAME, AnalyticsConstants.CREATE_WALLET_NAME, AnalyticsConstants.CREATE_WALLET_CHAIN);
    }

    public void logCreateWalletFiatClick() {
        logEvent(AnalyticsConstants.CREATE_WALLET_NAME, AnalyticsConstants.CREATE_WALLET_NAME, AnalyticsConstants.CREATE_WALLET_FIAT_CLICKED);
    }

//    public void logCreateWalletFiatSelect() {
//        logEvent(AnalyticsConstants.CREATE_WALLET_NAME, AnalyticsConstants.CREATE_WALLET_NAME, AnalyticsConstants.CREATE_WALLET_FIAT_SELECTED);
//    }

    public void logCreateWalletClose() {
        logEvent(AnalyticsConstants.CREATE_WALLET_NAME, AnalyticsConstants.CREATE_WALLET_NAME, AnalyticsConstants.CREATE_WALLET_CANCEL);
    }


    public void logSeedPhraseLaunch() {
        analytics.logEvent(AnalyticsConstants.SEED_PHRASE, null);
    }

    public void logSeedPhraseClose() {
        logEvent(AnalyticsConstants.SEED_PHRASE_NAME, AnalyticsConstants.SEED_PHRASE_NAME, AnalyticsConstants.SEED_PHRASE_CLOSE);
    }

    public void logSeedPhraseRepeat() {
        logEvent(AnalyticsConstants.SEED_PHRASE_NAME, AnalyticsConstants.SEED_PHRASE_NAME, AnalyticsConstants.SEED_PHRASE_REPEAT);
    }


    public void logRestoreSeedLaunch() {
        analytics.logEvent(AnalyticsConstants.SEED_PHRASE_RESTORE, null);
    }

    public void logRestoreSeedClose() {
        logEvent(AnalyticsConstants.SEED_PHRASE_RESTORE_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_CANCEL);
    }


    public void logSeedSuccessLaunch() {
        analytics.logEvent(AnalyticsConstants.SEED_PHRASE_RESTORE_SUCCESS, null);
    }

    public void logSeedSuccessClose() {
        logEvent(AnalyticsConstants.SEED_PHRASE_RESTORE_SUCCESS_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_SUCCESS_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_SUCCESS_CANCEL);
    }

    public void logSeedSuccessOk() {
        logEvent(AnalyticsConstants.SEED_PHRASE_RESTORE_SUCCESS_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_SUCCESS_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_SUCCESS_OK);
    }

    public void logSeedBackuped() {
        logEvent(AnalyticsConstants.SEED_PHRASE_RESTORE_SUCCESS_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_SUCCESS_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_SUCCESS_BACKUP);
    }


    public void logSeedFailLaunch() {
        analytics.logEvent(AnalyticsConstants.SEED_PHRASE_RESTORE_FAIL, null);
    }

    public void logSeedFailClose() {
        logEvent(AnalyticsConstants.SEED_PHRASE_RESTORE_FAIL_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_FAIL_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_FAIL_CANCEL);
    }

    public void logSeedFailTryAgain() {
        logEvent(AnalyticsConstants.SEED_PHRASE_RESTORE_FAIL_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_FAIL_NAME, AnalyticsConstants.SEED_PHRASE_RESTORE_TRY_AGAIN);
    }

    public void logSeedFailBackup() {
        analytics.logEvent(AnalyticsConstants.SEED_PHRASE_RESTORE_FAIL_BACKUP, null);
    }


    public void logMainLaunch() {
        analytics.logEvent(AnalyticsConstants.MAIN_SCREEN, null);
    }

    public void logMain(String argument) {
        logEvent(AnalyticsConstants.MAIN_SCREEN_NAME, AnalyticsConstants.MAIN_SCREEN_NAME, argument);
    }

    public void logMainWalletOpen(int chainId) {
        logEvent(AnalyticsConstants.MAIN_SCREEN_NAME, AnalyticsConstants.MAIN_WALLET_CLICK, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }


    public void logActivityLaunch() {
        analytics.logEvent(AnalyticsConstants.ACTIVITY_SCREEN, null);
    }

    public void logActivityClose() {
        logEvent(AnalyticsConstants.ACTIVITY_SCREEN_NAME, AnalyticsConstants.ACTIVITY_SCREEN_NAME, AnalyticsConstants.BUTTON_CLOSE);
    }

    public void logContactsLaunch() {
        analytics.logEvent(AnalyticsConstants.CONTACTS_SCREEN, null);
    }

    public void logContactsClose() {
        logEvent(AnalyticsConstants.CONTACTS_SCREEN_NAME, AnalyticsConstants.CONTACTS_SCREEN_NAME, AnalyticsConstants.BUTTON_CLOSE);
    }

    public void logFastOperationsLaunch() {
        analytics.logEvent(AnalyticsConstants.FAST_OPERATIONS, null);
    }

    public void logFastOperations(String argument) {
        logEvent(AnalyticsConstants.FAST_OPERATIONS_NAME, AnalyticsConstants.FAST_OPERATIONS_NAME, argument);
    }


    public void logScanQRLaunch() {
        analytics.logEvent(AnalyticsConstants.QR_SCREEN, null);
    }

    public void logScanQRClose() {
        logEvent(AnalyticsConstants.QR_SCREEN_NAME, AnalyticsConstants.QR_SCREEN_NAME, AnalyticsConstants.BUTTON_CLOSE);
    }


    public void logSettingsLaunch() {
        analytics.logEvent(AnalyticsConstants.SETTINGS_SCREEN, null);
    }

    public void logSettings(String argument) {
        logEvent(AnalyticsConstants.SETTINGS_SCREEN_NAME, AnalyticsConstants.SETTINGS_SCREEN_NAME, argument);
    }


    public void logSecuritySettingsLaunch() {
        analytics.logEvent(AnalyticsConstants.SECURITY_SETTINGS_SCREEN, null);
    }

    public void logSecuritySettings(String argument) {
        logEvent(AnalyticsConstants.SECURITY_SETTINGS_SCREEN_NAME, AnalyticsConstants.SECURITY_SETTINGS_SCREEN_NAME, argument);
    }


    public void logEntranceSettingsLaunch() {
        analytics.logEvent(AnalyticsConstants.ENTRANCE_SETTINGS_SCREEN, null);
    }

    public void logEntranceSettings(String argument) {
        logEvent(AnalyticsConstants.ENTRANCE_SETTINGS_SCREEN_NAME, AnalyticsConstants.ENTRANCE_SETTINGS_SCREEN_NAME, argument);
    }


    public void logWalletLaunch(String argument, int chainId) {
        logEvent(argument, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logWallet(String argument, int chainId) {
        logEvent(AnalyticsConstants.WALLET_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logWalletSharing(int chainId, String appName) {
        logEvent(AnalyticsConstants.WALLET_SCREEN_NAME, AnalyticsConstants.SHARED_WITH, String.format(Locale.US, AnalyticsConstants.CHAIN_ID_APP_NAME, chainId, appName));
    }

    public void logWalletBackup(String argument) {
        logEvent(AnalyticsConstants.WALLET_SCREEN_NAME, AnalyticsConstants.WALLET_SCREEN_NAME, argument);
    }


    public void logWalletTransactionLaunch(String argument, int chainId, int state) {
        logEvent(argument, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID_STATE, chainId, state));
    }

    public void logWalletTransaction(String argument, int chainId) {
        logEvent(AnalyticsConstants.WALLET_TRANSACTIONS_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logWalletTransactionBlockchain(String argument, int chainId, int state) {
        logEvent(AnalyticsConstants.WALLET_TRANSACTIONS_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID_STATE, chainId, state));
    }


    public void logWalletAddressesLaunch(int chainId) {
        logEvent(AnalyticsConstants.WALLET_ADDRESSES_SCREEN, AnalyticsConstants.WALLET_ADDRESSES_SCREEN, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logWalletAddresses(String argument, int chainId) {
        logEvent(AnalyticsConstants.WALLET_ADDRESSES_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }


    public void logWalletSettingsLaunch(int chainId) {
        logEvent(AnalyticsConstants.WALLET_SETTINGS_SCREEN, AnalyticsConstants.WALLET_SETTINGS_SCREEN, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logWalletSettings(String argument, int chainId) {
        logEvent(AnalyticsConstants.WALLET_SETTINGS_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }


    public void logNoInternetLaunch() {
        analytics.logEvent(AnalyticsConstants.NO_INTERNET_SCREEN, null);
    }

    public void logNoInternet(String argument) {
        logEvent(AnalyticsConstants.NO_INTERNET_SCREEN_NAME, AnalyticsConstants.NO_INTERNET_SCREEN_NAME, argument);
    }


    public void logSendToLaunch() {
        analytics.logEvent(AnalyticsConstants.SEND_TO_SCREEN, null);
    }

    public void logSendTo(String argument) {
        logEvent(AnalyticsConstants.SEND_TO_SCREEN_NAME, AnalyticsConstants.SEND_TO_SCREEN_NAME, argument);
    }


    public void logSendFromLaunch() {
        analytics.logEvent(AnalyticsConstants.SEND_FROM_SCREEN, null);
    }

    public void logSendFrom(String argument, int chainId) {
        logEvent(AnalyticsConstants.SEND_FROM_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }


    public void logTransactionFeeLaunch(int chainId) {
        logEvent(AnalyticsConstants.TRANSACTION_FEE_SCREEN, AnalyticsConstants.TRANSACTION_FEE_SCREEN, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logTransactionFee(String argument, int chainId) {
        logEvent(AnalyticsConstants.TRANSACTION_FEE_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }


    public void logSendChooseAmountLaunch(int chainId) {
        logEvent(AnalyticsConstants.SEND_AMOUNT_SCREEN, AnalyticsConstants.SEND_AMOUNT_SCREEN, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logSendChooseAmount(String argument, int chainId) {
        logEvent(AnalyticsConstants.SEND_AMOUNT_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logSendSummaryLaunch(int chainId) {
        logEvent(AnalyticsConstants.SEND_SUMMARY_SCREEN, AnalyticsConstants.SEND_SUMMARY_SCREEN, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logSendSummary(String argument, int chainId) {
        logEvent(AnalyticsConstants.SEND_SUMMARY_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }


    public void logSendSuccessLaunch(int chainId) {
        logEvent(AnalyticsConstants.SEND_SUCCESS_SCREEN, AnalyticsConstants.SEND_SUCCESS_SCREEN, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logSendSuccess(String argument, int chainId) {
        logEvent(AnalyticsConstants.SEND_SUCCESS_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }


    public void logReceiveLaunch(int chainId) {
        logEvent(AnalyticsConstants.RECEIVE_SCREEN, AnalyticsConstants.RECEIVE_SCREEN, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logReceive(String argument, int chainId) {
        logEvent(AnalyticsConstants.RECEIVE_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logReceiveSharing(int chainId, String appName) {
        logEvent(AnalyticsConstants.RECEIVE_SCREEN_NAME, AnalyticsConstants.SHARED_WITH, String.format(Locale.US, AnalyticsConstants.CHAIN_ID_APP_NAME, chainId, appName));
    }


    public void logReceiveSummaryLaunch(int chainId) {
        logEvent(AnalyticsConstants.RECEIVE_SUMMARY_SCREEN, AnalyticsConstants.RECEIVE_SUMMARY_SCREEN, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logReceiveSummary(String argument, int chainId) {
        logEvent(AnalyticsConstants.RECEIVE_SUMMARY_SCREEN_NAME, argument, String.format(Locale.US, AnalyticsConstants.CHAIN_ID, chainId));
    }

    public void logPush(String argument, String pushId) {
        logEvent(argument, argument, String.format(Locale.US, AnalyticsConstants.PUSH_ID, pushId));
    }

    public void logError(String argument) {
        logEvent(argument, argument, null);
    }

    public void logDonationAllertLaunch(int featureId) {
        logEvent(AnalyticsConstants.DONATION_ALERT, AnalyticsConstants.DONATION_ALERT, String.format(Locale.US, AnalyticsConstants.FEATURE_ID, featureId));
    }

    public void logDonationAllertClose(int featureId) {
        logEvent(AnalyticsConstants.DONATION_ALERT_NAME, AnalyticsConstants.BUTTON_CLOSE, String.format(Locale.US, AnalyticsConstants.FEATURE_ID, featureId));
    }

    public void logDonationAllertDonateClick(int featureId) {
        logEvent(AnalyticsConstants.DONATION_ALERT_NAME, AnalyticsConstants.BUTTON_DONATE, String.format(Locale.US, AnalyticsConstants.FEATURE_ID, featureId));
    }

    public void logDonationSendLaunch(int featureId) {
        logEvent(AnalyticsConstants.DONATION_SEND_SCREEN, AnalyticsConstants.DONATION_SEND_SCREEN, String.format(Locale.US, AnalyticsConstants.FEATURE_ID, featureId));
    }

    public void logDonationSendDonateClick(int featureId) {
        logEvent(AnalyticsConstants.DONATION_SEND_SCREEN_NAME, AnalyticsConstants.BUTTON_SEND_DONATE, String.format(Locale.US, AnalyticsConstants.FEATURE_ID, featureId));
    }

    public void logDonationSuccessLaunch(int featureId) {
        logEvent(AnalyticsConstants.DONATION_SUCCESS_SCREEN, AnalyticsConstants.DONATION_SUCCESS_SCREEN, String.format(Locale.US, AnalyticsConstants.FEATURE_ID, featureId));
    }

    public void logEvent(String event, String argumentName, String argument) {
        Bundle bundle = new Bundle();
        bundle.putString(argumentName, argument);
        analytics.logEvent(event, bundle);
    }


}
