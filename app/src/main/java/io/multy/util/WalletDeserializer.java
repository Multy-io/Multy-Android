/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.multy.model.entities.wallet.BtcWallet;
import io.multy.model.entities.wallet.EosWallet;
import io.multy.model.entities.wallet.EthWallet;
import io.multy.model.entities.wallet.Wallet;

public class WalletDeserializer implements JsonDeserializer<List<Wallet>> {

    @Override
    public List<Wallet> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<Wallet> wallets = new ArrayList<>();
        Gson gson = new Gson();
        try {
            JSONArray jsonArray = new JSONArray(json.toString());
            int currencyId;
            String jsonItemString;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonItemString = jsonArray.getJSONObject(i).toString();
                Wallet wallet = gson.fromJson(jsonItemString, Wallet.class);
                currencyId = wallet.getCurrencyId();

                switch (NativeDataHelper.Blockchain.valueOf(currencyId)) {
                    case ETH:
                        EthWallet ethWallet = gson.fromJson(jsonItemString, EthWallet.class);
                        wallet.setBalance(ethWallet.getBalance());
                        wallet.setAvailableBalance(ethWallet.getBalance());
                        wallet.setEthWallet(ethWallet);
                        break;
                    case BTC:
                        BtcWallet btcWallet = gson.fromJson(jsonItemString, BtcWallet.class);
                        wallet.setAvailableBalance(String.valueOf(btcWallet.calculateAvailableBalance()));
                        wallet.setBalance(String.valueOf(btcWallet.calculateBalance()));
                        wallet.setBtcWallet(btcWallet);
                        break;
                    case EOS:
                        EosWallet eosWallet = gson.fromJson(jsonItemString, EosWallet.class);
                        String balance = eosWallet.getBalance();
                        wallet.setAvailableBalance(balance);
                        wallet.setBalance(balance);
                        wallet.setEosWallet(eosWallet);
                        break;
                }
                wallets.add(wallet);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return wallets;
    }
}
