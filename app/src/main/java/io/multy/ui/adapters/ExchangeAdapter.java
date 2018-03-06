/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;

/**
 * Created by anschutz1927@gmail.com on 05.03.18.
 */

public class ExchangeAdapter extends RecyclerView.Adapter<ExchangeAdapter.Holder> {

    private ExchangeType type;
    private Listener listener;
    private String[] exchangeSoonName;
    private String currentExchange;
    private String[] exchangeAvailableName;

    public ExchangeAdapter(ExchangeType type, Listener listener) {
        this.type = type;
        this.listener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (type) {
            case AVAILABLE:
                return new AvailableHolder(inflater.inflate(R.layout.item_exchange_availabe, parent, false));
            case SOON:
                return new SoonHolder(inflater.inflate(R.layout.item_exchange_soon, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        switch (type) {
            case AVAILABLE:
                ((AvailableHolder) holder).checkBox.setChecked(exchangeAvailableName[position].equals(currentExchange));
                holder.textExchange.setText(exchangeAvailableName[position]);
                ((AvailableHolder) holder).itemView.setOnClickListener(v ->
                        listener.onAvailableExchangeClick(exchangeAvailableName[position]));
                holder.divider.setVisibility(exchangeAvailableName.length - 1 == position ? View.INVISIBLE : View.VISIBLE);
                break;
            case SOON:
                holder.textExchange.setText(exchangeSoonName[position]);
                holder.itemView.setOnClickListener(v -> listener.onSoonExchangeClick(exchangeSoonName[position]));
                holder.divider.setVisibility(exchangeSoonName.length - 1 == position ? View.INVISIBLE : View.VISIBLE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        switch (type) {
            case AVAILABLE:
                return exchangeAvailableName == null ? 0 : exchangeAvailableName.length;
            case SOON:
                return exchangeSoonName == null ? 0 : exchangeSoonName.length;
        }
        return 0;
    }

    public void setAvailableData(String currentExchange, String[] exchangeAvailableName) {
        this.currentExchange = currentExchange;
        this.exchangeAvailableName = exchangeAvailableName;
        notifyDataSetChanged();
    }

    public void setSoonData(String[] exchangeSoonName) {
        this.exchangeSoonName = exchangeSoonName;
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
        }
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_exchange)
        TextView textExchange;
        @BindView(R.id.divider)
        View divider;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public enum ExchangeType {AVAILABLE, SOON}

    public interface Listener {
        void onAvailableExchangeClick(String clickedChainName);
        void onSoonExchangeClick(String clickedChainName);
    }
}
