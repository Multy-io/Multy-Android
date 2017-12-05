/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.Wallet;
import io.multy.util.Constants;


public class BitcoinWallet extends Wallet {

    public BitcoinWallet(String name, String address, double balance) {
        super(name, address, balance);
    }

    @Override
    public String getBalanceWithCode() {
        return String.valueOf(getBalance()).concat(Constants.SPACE).concat(CurrencyCode.BTC.name());
    }
}
