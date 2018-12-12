/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.requests;

import java.util.List;

import io.multy.model.entities.wallet.DiscoverableWalletInfo;

public class DiscoverWalletRequest {

    private List<DiscoverableWalletInfo> walletInfoList;
    private int currencyId;
    private int networkId;

    public DiscoverWalletRequest(List<DiscoverableWalletInfo> walletInfoList, int currencyId, int networkId) {
        this.walletInfoList = walletInfoList;
        this.currencyId = currencyId;
        this.networkId = networkId;
    }
}
