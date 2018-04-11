/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;
import java.util.List;


public class UserAssetsResponse {

    private List<WalletInfo> walletInfo;

    public List<WalletInfo> getWalletInfo() {
        return walletInfo;
    }

    public void setWalletInfo(List<WalletInfo> walletInfo) {
        this.walletInfo = walletInfo;
    }
}
