/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

public interface WalletBalanceInterface {

    String getBalanceLabel();

    String getFiatBalanceLabel();

    int getIconResourceId();
}
