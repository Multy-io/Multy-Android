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
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.RecentAddress;
import io.multy.util.NativeDataHelper;
import io.realm.RealmResults;

public class RecentAddressesAdapter extends RecyclerView.Adapter<RecentAddressesAdapter.RecentAddressHolder> {

    public interface OnRecentAddressClickListener {
        void onClickRecentAddress(String address);
    }

    private RealmResults<RecentAddress> data;
    private OnRecentAddressClickListener listener;

    public RecentAddressesAdapter(RealmResults<RecentAddress> data, OnRecentAddressClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    public RecentAddressHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecentAddressHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_address, parent, false));
    }

    @Override
    public void onBindViewHolder(RecentAddressHolder holder, int position) {
        holder.textAddress.setText(data.get(position).getAddress());
        holder.itemView.setOnClickListener(v -> listener.onClickRecentAddress(data.get(position).getAddress()));
        holder.imageCurrency.setImageResource(data.get(position).getNetworkId() == NativeDataHelper.NetworkId.MAIN_NET.getValue() ?
                R.drawable.ic_btc_huge : R.drawable.ic_chain_btc_test);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    class RecentAddressHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_currency)
        ImageView imageCurrency;
        @BindView(R.id.text_address)
        TextView textAddress;

        RecentAddressHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
