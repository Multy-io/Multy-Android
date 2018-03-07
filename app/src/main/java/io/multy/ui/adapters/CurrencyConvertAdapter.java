/*
 * Copyright 2017 Idealnaya rabota LLC
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
                return new DiasabledCurrencyHolder(inflater.inflate(R.layout.item_chain_soon, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        switch (chainType) {
            case AVAILABLE:
                ((AvailableCurrencyHolder) holder).image.setImageDrawable(availableCurrencyImageIds.getDrawable(position));
                ((AvailableCurrencyHolder) holder).textAbbrev.setText(availableCurrencyAbbrevs[position]);
                ((AvailableCurrencyHolder) holder).textName.setText(availableCurencyNames[position]);
                ((AvailableCurrencyHolder) holder).checkBox
                        .setChecked(currency != null && availableCurrencyAbbrevs[position].equals(currency));
                holder.divider.setVisibility(position == availableCurrencyAbbrevs.length - 1 ? View.INVISIBLE : View.VISIBLE);
                holder.itemView.setOnClickListener(v -> listener.onClickAvailableCurrency(availableCurrencyAbbrevs[position]));
                break;
            case SOON:
                ((DiasabledCurrencyHolder) holder).image.setImageDrawable(disabledCurrencyImageIds.getDrawable(position));
                ((DiasabledCurrencyHolder) holder).textAbbrev.setText(disabledCurrencyAbbrevs[position]);
                ((DiasabledCurrencyHolder) holder).textName.setText(disabledCurrencyNames[position]);
                holder.divider.setVisibility(position == disabledCurrencyAbbrevs.length - 1 ? View.INVISIBLE : View.VISIBLE);
                holder.itemView.setOnClickListener(v -> listener.onClickDisabledCurrency(disabledCurrencyAbbrevs[position]));
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

    public void setAvailableChainsData(String chainCurrency, TypedArray chainsAvailableImageIds, String[] chainsAvailableAbbrev, String[] chainsAvailableName) {
        this.currency = chainCurrency;
        this.availableCurrencyImageIds = chainsAvailableImageIds;
        this.availableCurrencyAbbrevs = chainsAvailableAbbrev;
        this.availableCurencyNames = chainsAvailableName;
        notifyDataSetChanged();
    }

    public void setSoonChainsData(TypedArray chainsSoonImageIds, String[] chainsSoonAbbrev, String[] chainsSoonName) {
        this.disabledCurrencyImageIds = chainsSoonImageIds;
        this.disabledCurrencyAbbrevs = chainsSoonAbbrev;
        this.disabledCurrencyNames = chainsSoonName;
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

    class DiasabledCurrencyHolder extends Holder {

        DiasabledCurrencyHolder(View itemView) {
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
        void onClickDisabledCurrency(String clickedDisabledCurrencyName);
    }
}
