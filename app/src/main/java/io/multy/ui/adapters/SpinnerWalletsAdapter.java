/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.multy.R;
import io.multy.model.entities.wallet.Wallet;

public class SpinnerWalletsAdapter extends ArrayAdapter<Wallet> {

    private Wallet[] wallets;

    public SpinnerWalletsAdapter(@NonNull Context context, int resource, @NonNull Wallet[] objects) {
        super(context, resource, objects);

        wallets = objects;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, parent);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, parent);
    }

    private View createView(int position, ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spinner_wallet, parent, false);

        Wallet wallet = wallets[position];
        TextView name = view.findViewById(R.id.text_name);
        TextView balance = view.findViewById(R.id.text_balance);
        TextView balanceFiat = view.findViewById(R.id.text_balance_fiat);

        name.setText(wallet.getWalletName());
        balance.setText(wallet.getAvailableBalanceLabel());
        balanceFiat.setText(wallet.getAvailableFiatBalanceLabel());

        return view;
    }
}
