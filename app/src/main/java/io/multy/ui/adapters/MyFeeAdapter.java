/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;


import android.support.annotation.Nullable;
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
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;

public class MyFeeAdapter extends RecyclerView.Adapter<MyFeeAdapter.FeeHolder> {

    public enum FeeType {
        ETH, BTC
    }

    private ArrayList<Fee> rates;
    private OnCustomFeeClickListener listener;
    private FeeType feeType;

    public MyFeeAdapter(ArrayList<Fee> rates, @Nullable Fee selectedFee,
                        OnCustomFeeClickListener listener, FeeType feeType) {
        this.rates = rates;
        this.listener = listener;
        this.feeType = feeType;
        initRates(selectedFee);
    }

    @Override
    public FeeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FeeHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fee, parent, false));
    }

    @Override
    public void onBindViewHolder(FeeHolder holder, int position) {
        Fee rate = rates.get(position);
        long price;
        double ethPrice;
        holder.imageMark.setVisibility(rate.isSelected() ? View.VISIBLE : View.INVISIBLE);
        switch (feeType) {
            case BTC:
                price = (rate.getAmount() == 0 ? 1000 : rate.getAmount()) * Constants.BTC_TRANSACTION_SIZE;
                if (position == rates.size() - 1) {
                    holder.textName.setText(rate.getName());
                    holder.divider.setVisibility(View.GONE);
                    holder.imageLogo.setImageResource(R.drawable.ic_custom);
                    holder.textBalanceOriginal.setText(String.format("~%s BTC / ~%s USD",
                                CryptoFormatUtils.satoshiToBtc(price), CryptoFormatUtils.satoshiToUsd(price)));
                    holder.root.setOnClickListener(v -> listener.onClickCustomFee(rate.getAmount()));
                } else {
                    holder.imageLogo.setImageResource(getIconResId(position));
                    holder.textName.setText(rate.getName());
                    holder.textBalanceOriginal.setText(String.format("~%s BTC / ~%s USD",
                            CryptoFormatUtils.satoshiToBtc(price), CryptoFormatUtils.satoshiToUsd(price)));
                    holder.root.setOnClickListener(v -> {
                        setItemSelected(position);
                        listener.logTransactionFee(position);
                    });
                }

                break;
            case ETH:
                ethPrice = Math.abs(CryptoFormatUtils.weiToEth(String.valueOf(rate.getAmount())));
                String stringEth = CryptoFormatUtils.FORMAT_ETH.format(ethPrice);
                if (position == rates.size() - 1) {
                    holder.textName.setText(rate.getName());
                    holder.divider.setVisibility(View.GONE);
                    holder.imageLogo.setImageResource(R.drawable.ic_custom);
                    if (ethPrice <= 0) {
                        holder.textBalanceOriginal.setVisibility(View.GONE);
                    } else {
                        holder.textBalanceOriginal.setText(String.format("%s ETH", stringEth));
                    }
                    holder.root.setOnClickListener(v -> listener.onClickCustomFee(rate.getAmount()));
                } else {
                    holder.imageLogo.setImageResource(getIconResId(position));
                    holder.textName.setText(String.format("%s · %s", rate.getName(), rate.getTime()));
                    holder.textBalanceOriginal.setText(String.format("%s ETH", stringEth));
                    holder.root.setOnClickListener(v -> {
                        setItemSelected(position);
                        listener.logTransactionFee(position);
                    });
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return rates.size();
    }

    private void initRates(Fee selectedFee) {
        if (rates == null) {
            return;
        }
        if (rates.size() > 1) {
            rates.get(rates.size() - 1).setAmount(rates.get(rates.size() - 2).getAmount());
        }
        if (selectedFee != null) {
            for (int i = 0; i < rates.size(); i++) {
                if (rates.get(i).getName().equals(selectedFee.getName())) {
                    rates.get(i).setSelected(true);
                    if (i == rates.size() - 1) {
                        rates.get(i).setAmount(selectedFee.getAmount());
                    }
                    break;
                }
            }
        } else {
            int serverFeeCount = rates.size() - 2;
            int centerServerFee = serverFeeCount / 2;
            rates.get(centerServerFee).setSelected(true);
        }
    }

    private void setItemSelected(int position) {
        for (int i = 0; i < rates.size(); i++) {
            rates.get(i).setSelected(i == position);
        }
        listener.onClickFee(rates.get(position));
        notifyDataSetChanged();
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

    @Nullable
    public Fee getSelectedFee() {
        for (Fee rate : rates) {
            if (rate.isSelected()) {
                return rate;
            }
        }
        return null;
    }

    public void setCustomFee(long fee) {
        rates.get(rates.size() - 1).setAmount(fee);
        setItemSelected(rates.size() - 1);
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

    public interface OnCustomFeeClickListener {
        void onClickFee(Fee fee);
        void onClickCustomFee(long currentValue);
        void logTransactionFee(int position);
    }
}
