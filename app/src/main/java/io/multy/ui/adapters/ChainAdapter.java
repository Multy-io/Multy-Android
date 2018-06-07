/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.content.res.TypedArray;
import android.support.annotation.NonNull;
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
    private final OnItemClickListener listener;
    private int chainNet;
    private String chainCurrency;
    private TypedArray availableChainsImageIds;
    private String[] availableChainsAbbrev;
    private String[] availableChainsName;
    private int[] availableChainNets;
    private int[] availableChainIds;
    private TypedArray disabledChainsImageIds;
    private String[] disabledChainsAbbrev;
    private String[] disabledChainsName;
    private TypedArray disabledChainsDonationCodes;

    public ChainAdapter(ChainType chainType, OnItemClickListener listener) {
        this.chainType = chainType;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (chainType) {
            case AVAILABLE:
                return new AvailableChainHolder(inflater.inflate(R.layout.item_chain_availabe,parent, false));
            case SOON:
                return new DisabledChainHolder(inflater.inflate(R.layout.item_chain_soon, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        switch (chainType) {
            case AVAILABLE:
                bindAvailable((AvailableChainHolder) holder, position);
                break;
            case SOON:
                bindDisable((DisabledChainHolder) holder, position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        switch (chainType) {
            case AVAILABLE:
                return availableChainsName == null ? 0 : availableChainsName.length;
            case SOON:
                return disabledChainsName == null ? 0 : disabledChainsName.length;
        }
        return 0;
    }

    public void setAvailableChainsData(int chainNet, String chainCurrency, TypedArray chainsAvailableImageIds,
                                       String[] chainsAvailableAbbrev, String[] chainsAvailableName,
                                       int[] availableChainNets, int[] availableChainIds) {
        this.chainNet = chainNet;
        this.chainCurrency = chainCurrency;
        this.availableChainsImageIds = chainsAvailableImageIds;
        this.availableChainsAbbrev = chainsAvailableAbbrev;
        this.availableChainsName = chainsAvailableName;
        this.availableChainNets = availableChainNets;
        this.availableChainIds = availableChainIds;
        notifyDataSetChanged();
    }

    private void bindAvailable(AvailableChainHolder holder, int position) {
        holder.imageCoin.setImageDrawable(availableChainsImageIds.getDrawable(position));
        holder.textChainAbbrev.setText(availableChainsAbbrev[position]);
        holder.textChainName.setText(availableChainsName[position]);
        holder.checkBox.setChecked(chainCurrency != null
                && availableChainsAbbrev[position].equals(chainCurrency) && chainNet == availableChainNets[position]);
        holder.divider.setVisibility(position == availableChainsAbbrev.length - 1 ? View.INVISIBLE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> {
            String selectedName = availableChainsName[position] + "・" + availableChainsAbbrev[position];
            listener.onClickAvailableChain(selectedName, availableChainNets[position], availableChainIds[position]);
        });
    }

    private void bindDisable(DisabledChainHolder holder, int position) {
        holder.imageCoin.setImageDrawable(disabledChainsImageIds.getDrawable(position));
        holder.textChainAbbrev.setText(disabledChainsAbbrev[position]);
        holder.textChainName.setText(disabledChainsName[position]);
        holder.divider.setVisibility(position == disabledChainsAbbrev.length - 1 ? View.INVISIBLE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> listener.onClickSoonChain(disabledChainsAbbrev[position],
                disabledChainsDonationCodes.getInteger(position, 0)));
    }

    public void setSoonChainsData(TypedArray disableChainImageIds, String[] disabledChainAbbrevs,
                                  String[] disabledChainNames, TypedArray disabledChainDonationCodes) {
        this.disabledChainsImageIds = disableChainImageIds;
        this.disabledChainsAbbrev = disabledChainAbbrevs;
        this.disabledChainsName = disabledChainNames;
        this.disabledChainsDonationCodes = disabledChainDonationCodes;
        notifyDataSetChanged();
    }

    class AvailableChainHolder extends Holder {

        @BindView(R.id.checkbox)
        CheckBox checkBox;

        AvailableChainHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class DisabledChainHolder extends Holder {

        DisabledChainHolder(View itemView) {
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

    public interface OnItemClickListener {
        void onClickAvailableChain(String clickedChainName, int clickedChainNet, int clickedChainId);
        void onClickSoonChain(String clickedChainName, int donationCode);
    }
}
