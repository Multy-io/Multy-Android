/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;

/**
 * Created by anschutz1927@gmail.com on 03.03.18.
 */

public class CurrencyConvertAdapter extends RecyclerView.Adapter<CurrencyConvertAdapter.Holder> {

    private final CurrencyType chainType;
    private final OnItemClickListener listener;
    private String currency;
    private TypedArray availableCurrencyImageIds;
    private String[] availableCurrencyAbbrevs;
    private String[] availableCurencyNames;
    private TypedArray disabledCurrencyImageIds;
    private String[] disabledCurrencyAbbrevs;
    private String[] disabledCurrencyNames;
    private TypedArray disablesCurrencyDonationCodes;

    public CurrencyConvertAdapter(CurrencyType chainType, OnItemClickListener listener) {
        this.chainType = chainType;
        this.listener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (chainType) {
            case AVAILABLE:
                return new AvailableCurrencyHolder(inflater.inflate(R.layout.item_chain_availabe,parent, false));
            case SOON:
                return new DisabledCurrencyHolder(inflater.inflate(R.layout.item_chain_soon, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        switch (chainType) {
            case AVAILABLE:
                bindAvailable((AvailableCurrencyHolder) holder, position);
                break;
            case SOON:
                bindDisabled((DisabledCurrencyHolder) holder, position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        switch (chainType) {
            case AVAILABLE:
                return availableCurencyNames == null ? 0 : availableCurencyNames.length;
            case SOON:
                return disabledCurrencyNames == null ? 0 : disabledCurrencyNames.length;
        }
        return 0;
    }

    private void bindAvailable(AvailableCurrencyHolder holder, int position) {
        holder.image.setImageDrawable(availableCurrencyImageIds.getDrawable(position));
        holder.textAbbrev.setText(availableCurrencyAbbrevs[position]);
        holder.textName.setText(availableCurencyNames[position]);
        holder.checkBox
                .setChecked(currency != null && availableCurrencyAbbrevs[position].equals(currency));
        holder.divider.setVisibility(position == availableCurrencyAbbrevs.length - 1 ? View.INVISIBLE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> listener.onClickAvailableCurrency(availableCurrencyAbbrevs[position]));
    }

    private void bindDisabled(DisabledCurrencyHolder holder, int position) {
        holder.image.setImageDrawable(disabledCurrencyImageIds.getDrawable(position));
        holder.textAbbrev.setText(disabledCurrencyAbbrevs[position]);
        holder.textName.setText(disabledCurrencyNames[position]);
        holder.divider.setVisibility(position == disabledCurrencyAbbrevs.length - 1 ? View.INVISIBLE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> listener.onClickDisabledCurrency(disabledCurrencyAbbrevs[position],
                disablesCurrencyDonationCodes.getInteger(position, 0)));
    }

    public void setAvailableCurrenciesData(String chainCurrency, TypedArray availableCurrencyImageIds,
                                           String[] availableCurrencyAbbrev, String[] availableCurrencyName) {
        this.currency = chainCurrency;
        this.availableCurrencyImageIds = availableCurrencyImageIds;
        this.availableCurrencyAbbrevs = availableCurrencyAbbrev;
        this.availableCurencyNames = availableCurrencyName;
        notifyDataSetChanged();
    }

    public void setSoonCurrenciesData(TypedArray disabledCurrencyImageIds, String[] disableCurrencyAbbrev,
                                      String[] disabledCurrencyName, TypedArray disabledCurrencyDonationCodes) {
        this.disabledCurrencyImageIds = disabledCurrencyImageIds;
        this.disabledCurrencyAbbrevs = disableCurrencyAbbrev;
        this.disabledCurrencyNames = disabledCurrencyName;
        this.disablesCurrencyDonationCodes = disabledCurrencyDonationCodes;
        notifyDataSetChanged();
    }

    class AvailableCurrencyHolder extends Holder {

        @BindView(R.id.checkbox)
        CheckBox checkBox;

        AvailableCurrencyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class DisabledCurrencyHolder extends Holder {

        DisabledCurrencyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_chain)
        ImageView image;
        @BindView(R.id.text_chain_short)
        TextView textAbbrev;
        @BindView(R.id.text_chain_name)
        TextView textName;
        @BindView(R.id.divider)
        View divider;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public enum CurrencyType {AVAILABLE, SOON}

    public interface OnItemClickListener {
        void onClickAvailableCurrency(String clickedDisabledCurrencyName);
        void onClickDisabledCurrency(String clickedDisabledCurrencyName, int donationCode);
    }
}
