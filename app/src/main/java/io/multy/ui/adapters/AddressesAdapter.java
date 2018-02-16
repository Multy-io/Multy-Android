/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;


import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.DateHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;

public class AddressesAdapter extends RecyclerView.Adapter<AddressesAdapter.Holder> {

    private List<WalletAddress> addresses;
    private OnAddressClickListener listener;

    public AddressesAdapter(List<WalletAddress> addresses) {
        this.addresses = addresses;
    }

    @Override
    public AddressesAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressesAdapter.Holder(view);
    }

    @Override
    public void onBindViewHolder(AddressesAdapter.Holder holder, int position) {
        holder.bind(addresses.get(position));
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)
        ConstraintLayout root;
        @BindView(R.id.text_date)
        TextView textDate;
        @BindView(R.id.text_address)
        TextView textAddress;
        @BindView(R.id.text_amount)
        TextView textAmount;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final WalletAddress address) {
            textAddress.setText(address.getAddress());
            textAmount.setText(String.format("%s / %s$", String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(address.getAmount())), CryptoFormatUtils.satoshiToUsd(address.getAmount())));
            textDate.setText(DateHelper.DATE_FORMAT_ADDRESSES.format(new Date(address.getDate() * 1000L)));
//            textName.setText(wallet.getName());
//            textBalanceOriginal.setText(wallet.getBalanceWithCode());
//            textBalanceUsd.setText(wallet.getBalanceWithCode());
            root.setOnClickListener(view -> {
                Analytics.getInstance(textAddress.getContext()).logWalletAddresses(AnalyticsConstants.WALLET_ADDRESSES_CLICK, 1);
            });
        }
    }

    public interface OnAddressClickListener {
        void onAddressClick(WalletAddress address);
    }
}
