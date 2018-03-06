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

    private final ChainType chainType;
    private final OnClickListener listener;
    private String currency;
    private TypedArray currenciesAvailableImageIds;
    private String[] currenciesAvailableAbbrev;
    private String[] currenciesAvailableName;
    private TypedArray currenciesSoonImageIds;
    private String[] currenciesSoonAbbrev;
    private String[] currenciesSoonName;

    public CurrencyConvertAdapter(ChainType chainType, OnClickListener listener) {
        this.chainType = chainType;
        this.listener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (chainType) {
            case AVAILABLE:
                return new AvailableHolder(inflater.inflate(R.layout.item_chain_availabe,parent, false));
            case SOON:
                return new SoonHolder(inflater.inflate(R.layout.item_chain_soon, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        switch (chainType) {
            case AVAILABLE:
                ((AvailableHolder) holder).image.setImageDrawable(currenciesAvailableImageIds.getDrawable(position));
                ((AvailableHolder) holder).textAbbrev.setText(currenciesAvailableAbbrev[position]);
                ((AvailableHolder) holder).textName.setText(currenciesAvailableName[position]);
                ((AvailableHolder) holder).checkBox
                        .setChecked(currency != null && currenciesAvailableAbbrev[position].equals(currency));
                holder.divider.setVisibility(position == currenciesAvailableAbbrev.length - 1 ? View.INVISIBLE : View.VISIBLE);
                holder.itemView.setOnClickListener(v -> listener.onClickAvailableCurrency(currenciesAvailableAbbrev[position]));
                break;
            case SOON:
                ((SoonHolder) holder).image.setImageDrawable(currenciesSoonImageIds.getDrawable(position));
                ((SoonHolder) holder).textAbbrev.setText(currenciesSoonAbbrev[position]);
                ((SoonHolder) holder).textName.setText(currenciesSoonName[position]);
                holder.divider.setVisibility(position == currenciesSoonAbbrev.length - 1 ? View.INVISIBLE : View.VISIBLE);
                holder.itemView.setOnClickListener(v -> listener.onClickSoonCurrency(currenciesSoonAbbrev[position]));
                break;
        }
    }

    @Override
    public int getItemCount() {
        switch (chainType) {
            case AVAILABLE:
                return currenciesAvailableName == null ? 0 : currenciesAvailableName.length;
            case SOON:
                return currenciesSoonName == null ? 0 : currenciesSoonName.length;
        }
        return 0;
    }

    public void setAvailableChainsData(String chainCurrency, TypedArray chainsAvailableImageIds, String[] chainsAvailableAbbrev, String[] chainsAvailableName) {
        this.currency = chainCurrency;
        this.currenciesAvailableImageIds = chainsAvailableImageIds;
        this.currenciesAvailableAbbrev = chainsAvailableAbbrev;
        this.currenciesAvailableName = chainsAvailableName;
        notifyDataSetChanged();
    }

    public void setSoonChainsData(TypedArray chainsSoonImageIds, String[] chainsSoonAbbrev, String[] chainsSoonName) {
        this.currenciesSoonImageIds = chainsSoonImageIds;
        this.currenciesSoonAbbrev = chainsSoonAbbrev;
        this.currenciesSoonName = chainsSoonName;
        notifyDataSetChanged();
    }

    class AvailableHolder extends Holder {

        @BindView(R.id.checkbox)
        CheckBox checkBox;

        AvailableHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class SoonHolder extends Holder {

        SoonHolder(View itemView) {
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

    public enum ChainType {AVAILABLE, SOON}

    public interface OnClickListener {
        void onClickAvailableCurrency(String clickedChainName);
        void onClickSoonCurrency(String clickedChainName);
    }
}
