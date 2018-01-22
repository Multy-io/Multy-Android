/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import java.util.List;

import io.multy.model.entities.wallet.WalletRealmObject;


public class RestoreResponse {

    //TODO move to base response
    private String code;
    private String message;

    private List<WalletRealmObject> wallets;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<WalletRealmObject> getWallets() {
        return wallets;
    }

    @Override
    public String toString() {
        return "RestoreResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", wallets=" + wallets +
                '}';
    }
}
