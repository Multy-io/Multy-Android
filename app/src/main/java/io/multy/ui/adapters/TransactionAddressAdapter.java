/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.WalletAddress;

/**
 * Created by anschutz1927@gmail.com on 25.05.18.
 */
public class TransactionAddressAdapter extends RecyclerView.Adapter<TransactionAddressAdapter.Holder> {

    private final ArrayList<WalletAddress> addresses;
    private final OnClickListener listener;

    public TransactionAddressAdapter(ArrayList<WalletAddress> addresses, OnClickListener listener) {
        this.addresses = addresses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_address, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.textAddress.setText(addresses.get(position).getAddress());
    }

    @Override
    public int getItemCount() {
        return addresses == null ? 0 : addresses.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_address)
        TextView textAddress;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.text_address)
        void onClickAddress(View view) {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true), 300);
            listener.onClickAddress(addresses.get(getAdapterPosition()).getAddress());
        }
    }

    public interface OnClickListener {
        void onClickAddress(String address);
    }
}
