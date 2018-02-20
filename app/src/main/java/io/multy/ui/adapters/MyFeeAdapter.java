/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;


import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.Fee;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.reactivex.annotations.Nullable;

public class MyFeeAdapter extends RecyclerView.Adapter<MyFeeAdapter.FeeHolder> {

    public interface OnCustomFeeClickListener {
        void onClickCustomFee(long currentValue);
        void logTransaction(int position);
    }

    private ArrayList<Fee> rates;
    private OnCustomFeeClickListener listener;

    public MyFeeAdapter(ArrayList<Fee> rates, OnCustomFeeClickListener listener) {
        this.rates = rates;
        this.listener = listener;
    }

    @Override
    public FeeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FeeHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fee, parent, false));
    }

    @Override
    public void onBindViewHolder(FeeHolder holder, int position) {
        Fee rate = rates.get(position);
        long price = rate.getAmount() == 0 ? 1000 : rate.getAmount();

        if (position == rates.size() - 1) {
            holder.textName.setText(rate.getName());
            holder.divider.setVisibility(View.GONE);
            holder.imageLogo.setImageResource(R.drawable.ic_custom);
            holder.textBalanceOriginal.setText(price == -1 ? "" : String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(price)));
            holder.root.setOnClickListener(v -> listener.onClickCustomFee(rate.getAmount()));
        } else {
            holder.imageLogo.setImageResource(getIconResId(position));
            holder.textBlocks.setText(String.format("%d blocks", rate.getBlockCount()));
            holder.textName.setText(String.format("%s · %s", rate.getName(), rate.getTime()));
            holder.textBalanceOriginal.setText(String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(price)));
            holder.root.setOnClickListener(v -> {
                setItemSelected(position);
                listener.logTransaction(position);
            });
        }
        holder.imageMark.setVisibility(rate.isSelected() ? View.VISIBLE : View.INVISIBLE);
    }

    private void setItemSelected(int position) {
        for (int i = 0; i < rates.size(); i++) {
            rates.get(i).setSelected(i == position);
        }
        notifyDataSetChanged();
    }

    public void setItemSelected(String name) {

    }

    @Nullable
    public Fee getSelectedFee() {
        for (Fee rate : rates) {
            if (rate.isSelected()) {
                return rate;
            }
        }

        return null;
    }

    private int getIconResId(int position) {
        switch (position) {
            case 0:
                return R.drawable.ic_very_fast;
            case 1:
                return R.drawable.ic_fast;
            case 2:
                return R.drawable.ic_medium;
            case 3:
                return R.drawable.ic_slow;
            default:
                return R.drawable.ic_very_slow;
        }
    }

    public void setCustomFee(long fee) {
        if (fee < 2) {
            fee = 2;
        }
        rates.get(rates.size() - 1).setAmount(fee);
        setItemSelected(rates.size() - 1);
    }

    @Override
    public int getItemCount() {
        return rates.size();
    }


    class FeeHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)
        ConstraintLayout root;
        @BindView(R.id.image_logo)
        ImageView imageLogo;
        @BindView(R.id.textName)
        TextView textName;
        @BindView(R.id.text_balance_original)
        TextView textBalanceOriginal;
        @BindView(R.id.text_blocks)
        TextView textBlocks;
        @BindView(R.id.divider)
        View divider;
        @BindView(R.id.image_mark)
        ImageView imageMark;

        FeeHolder(View itemView) {
            super(itemView);
            setIsRecyclable(false);
            ButterKnife.bind(this, itemView);
        }
    }
}
