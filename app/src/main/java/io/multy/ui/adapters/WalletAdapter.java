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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.WalletRealmObject;


public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.WalletHolder> {

    private List<WalletRealmObject> wallets;
    private OnWalletClickListener listener;
    private Double exchangePrice;

    public WalletAdapter(Double exchangePrice, OnWalletClickListener listener) {
        this.listener = listener;
        this.exchangePrice = exchangePrice;
    }

    @Override
    public WalletHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet, parent, false);
        return new WalletHolder(view);
    }

    @Override
    public void onBindViewHolder(WalletHolder holder, int position) {
        holder.bind(wallets.get(position));
    }

    @Override
    public int getItemCount() {
        return wallets.size();
    }

    public void setWallets(List<WalletRealmObject> wallets){
        this.wallets = wallets;
        notifyDataSetChanged();
    }

    public void setExchangePrice(Double exchangePrice){
        this.exchangePrice = exchangePrice;
        notifyDataSetChanged();
    }


    public class WalletHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)
        ConstraintLayout root;
        @BindView(R.id.image_logo)
        ImageView imageLogo;
        @BindView(R.id.textName)
        TextView textName;
        @BindView(R.id.text_balance_original)
        TextView textBalanceOriginal;
        @BindView(R.id.text_balance_currency)
        TextView textBalanceUsd;

        public WalletHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final WalletRealmObject wallet) {
            textName.setText(wallet.getName());
            textBalanceOriginal.setText(wallet.getBalanceWithCode(CurrencyCode.BTC));
            if (exchangePrice != null) {
                textBalanceUsd.setText(wallet.getBalanceFiatWithCode(exchangePrice, CurrencyCode.USD));
            }
            root.setOnClickListener(view -> listener.onWalletClick(wallet));
        }
    }

    public interface OnWalletClickListener{
        void onWalletClick(WalletRealmObject wallet);
    }
}
