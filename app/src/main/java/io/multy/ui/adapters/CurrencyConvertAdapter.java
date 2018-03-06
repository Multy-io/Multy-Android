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
    private final Listener listener;
    private String currency;
    private TypedArray currenciesAvailableImageIds;
    private String[] currenciesAvailableAbbrev;
    private String[] currenciesAvailableName;
    private TypedArray currenciesSoonImageIds;
    private String[] currenciesSoonAbbrev;
    private String[] currenciesSoonName;

    public CurrencyConvertAdapter(ChainType chainType, Listener listener) {
        this.chainType = chainType;
        this.listener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (chainType) {
            case AVAILABLE:
                return new HolderAvailable(inflater.inflate(R.layout.item_chain_availabe,parent, false));
            case SOON:
                return new HolderSoon(inflater.inflate(R.layout.item_chain_soon, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        switch (chainType) {
            case AVAILABLE:
                ((HolderAvailable) holder).image.setImageDrawable(currenciesAvailableImageIds.getDrawable(position));
                ((HolderAvailable) holder).textAbbrev.setText(currenciesAvailableAbbrev[position]);
                ((HolderAvailable) holder).textName.setText(currenciesAvailableName[position]);
                ((HolderAvailable) holder).checkBox
                        .setChecked(currency != null && currenciesAvailableAbbrev[position].equals(currency));
                holder.divider.setVisibility(position == currenciesAvailableAbbrev.length - 1 ? View.INVISIBLE : View.VISIBLE);
                holder.itemView.setOnClickListener(v -> listener.onAvailableCurrencyClick(currenciesAvailableAbbrev[position]));
                break;
            case SOON:
                ((HolderSoon) holder).image.setImageDrawable(currenciesSoonImageIds.getDrawable(position));
                ((HolderSoon) holder).textAbbrev.setText(currenciesSoonAbbrev[position]);
                ((HolderSoon) holder).textName.setText(currenciesSoonName[position]);
                holder.divider.setVisibility(position == currenciesSoonAbbrev.length - 1 ? View.INVISIBLE : View.VISIBLE);
                holder.itemView.setOnClickListener(v -> listener.onSoonCurrencyClick(currenciesSoonAbbrev[position]));
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

    class HolderAvailable extends Holder {

        @BindView(R.id.checkbox)
        CheckBox checkBox;

        HolderAvailable(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class HolderSoon extends Holder {

        HolderSoon(View itemView) {
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

    public interface Listener {
        void onAvailableCurrencyClick(String clickedChainName);
        void onSoonCurrencyClick(String clickedChainName);
    }
}
