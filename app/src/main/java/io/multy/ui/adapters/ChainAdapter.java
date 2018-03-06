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

public class ChainAdapter extends RecyclerView.Adapter<ChainAdapter.Holder> {

    private final ChainType chainType;
    private final OnClickListener listener;
    private String chainCurrency;
    private TypedArray chainsAvailableImageIds;
    private String[] chainsAvailableAbbrev;
    private String[] chainsAvailableName;
    private TypedArray chainsSoonImageIds;
    private String[] chainsSoonAbbrev;
    private String[] chainsSoonName;

    public ChainAdapter(ChainType chainType, OnClickListener listener) {
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
                ((AvailableHolder) holder).imageCoin.setImageDrawable(chainsAvailableImageIds.getDrawable(position));
                ((AvailableHolder) holder).textChainAbbrev.setText(chainsAvailableAbbrev[position]);
                ((AvailableHolder) holder).textChainName.setText(chainsAvailableName[position]);
                ((AvailableHolder) holder).checkBox
                        .setChecked(chainCurrency != null && chainsAvailableAbbrev[position].equals(chainCurrency));
                holder.divider.setVisibility(position == chainsAvailableAbbrev.length - 1 ? View.INVISIBLE : View.VISIBLE);
                holder.itemView.setOnClickListener(v -> listener.onClickAvailableChain(chainsAvailableAbbrev[position]));
                break;
            case SOON:
                ((SoonHolder) holder).imageCoin.setImageDrawable(chainsSoonImageIds.getDrawable(position));
                ((SoonHolder) holder).textChainAbbrev.setText(chainsSoonAbbrev[position]);
                ((SoonHolder) holder).textChainName.setText(chainsSoonName[position]);
                holder.divider.setVisibility(position == chainsSoonAbbrev.length - 1 ? View.INVISIBLE : View.VISIBLE);
                holder.itemView.setOnClickListener(v -> listener.onClickSoonChain(chainsSoonAbbrev[position]));
                break;
        }
    }

    @Override
    public int getItemCount() {
        switch (chainType) {
            case AVAILABLE:
                return chainsAvailableName == null ? 0 : chainsAvailableName.length;
            case SOON:
                return chainsSoonName == null ? 0 : chainsSoonName.length;
        }
        return 0;
    }

    public void setAvailableChainsData(String chainCurrency, TypedArray chainsAvailableImageIds, String[] chainsAvailableAbbrev, String[] chainsAvailableName) {
        this.chainCurrency = chainCurrency;
        this.chainsAvailableImageIds = chainsAvailableImageIds;
        this.chainsAvailableAbbrev = chainsAvailableAbbrev;
        this.chainsAvailableName = chainsAvailableName;
        notifyDataSetChanged();
    }

    public void setSoonChainsData(TypedArray chainsSoonImageIds, String[] chainsSoonAbbrev, String[] chainsSoonName) {
        this.chainsSoonImageIds = chainsSoonImageIds;
        this.chainsSoonAbbrev = chainsSoonAbbrev;
        this.chainsSoonName = chainsSoonName;
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
        ImageView imageCoin;
        @BindView(R.id.text_chain_short)
        TextView textChainAbbrev;
        @BindView(R.id.text_chain_name)
        TextView textChainName;
        @BindView(R.id.divider)
        View divider;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public enum ChainType {AVAILABLE, SOON}

    public interface OnClickListener {
        void onClickAvailableChain(String clickedChainName);
        void onClickSoonChain(String clickedChainName);
    }
}
