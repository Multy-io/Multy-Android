/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.util.NativeDataHelper;
import io.realm.RealmList;

public class WalletAddressesAdapter extends RecyclerView.Adapter<WalletAddressesAdapter.ViewHolder> {

    private RealmList<WalletAddress> data;
    private String currency;

    public WalletAddressesAdapter(RealmList<WalletAddress> data, NativeDataHelper.Blockchain currency) {
        this.data = data;
        if (currency == NativeDataHelper.Blockchain.BTC) {
            this.currency = "BTC";
        } else {
            this.currency = "ETH";
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.amount.setText(data.get(position).getAmount() + " " + currency);
        holder.address.setText(data.get(position).getAddress());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_address)
        TextView address;

        @BindView(R.id.text_amount)
        TextView amount;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
