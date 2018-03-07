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
    private OnItemClickListener listener;
    private String[] disabledExchangeNames;
    private String currentExchange;
    private String[] availableExchangeNames;

    public ExchangeAdapter(ExchangeType type, OnItemClickListener listener) {
        this.type = type;
        this.listener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (type) {
            case AVAILABLE:
                return new AvailableExchangeHolder(inflater.inflate(R.layout.item_exchange_availabe, parent, false));
            case SOON:
                return new DisabledExchangeHolder(inflater.inflate(R.layout.item_exchange_soon, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        switch (type) {
            case AVAILABLE:
                ((AvailableExchangeHolder) holder).checkBox.setChecked(availableExchangeNames[position].equals(currentExchange));
                holder.textExchange.setText(availableExchangeNames[position]);
                ((AvailableExchangeHolder) holder).itemView.setOnClickListener(v ->
                        listener.onAvailableExchangeClick(availableExchangeNames[position]));
                holder.divider.setVisibility(availableExchangeNames.length - 1 == position ? View.INVISIBLE : View.VISIBLE);
                break;
            case SOON:
                holder.textExchange.setText(disabledExchangeNames[position]);
                holder.itemView.setOnClickListener(v -> listener.onDisabledExchangeClick(disabledExchangeNames[position]));
                holder.divider.setVisibility(disabledExchangeNames.length - 1 == position ? View.INVISIBLE : View.VISIBLE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        switch (type) {
            case AVAILABLE:
                return availableExchangeNames == null ? 0 : availableExchangeNames.length;
            case SOON:
                return disabledExchangeNames == null ? 0 : disabledExchangeNames.length;
        }
        return 0;
    }

    public void setAvailableData(String currentExchange, String[] exchangeAvailableName) {
        this.currentExchange = currentExchange;
        this.availableExchangeNames = exchangeAvailableName;
        notifyDataSetChanged();
    }

    public void setSoonData(String[] exchangeSoonName) {
        this.disabledExchangeNames = exchangeSoonName;
        notifyDataSetChanged();
    }

    class AvailableExchangeHolder extends Holder {

        @BindView(R.id.checkbox)
        CheckBox checkBox;

        AvailableExchangeHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class DisabledExchangeHolder extends Holder {
        DisabledExchangeHolder(View itemView) {
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

    public interface OnItemClickListener {
        void onAvailableExchangeClick(String clickedExchangeName);
        void onDisabledExchangeClick(String clickedExchangeName);
    }
}
