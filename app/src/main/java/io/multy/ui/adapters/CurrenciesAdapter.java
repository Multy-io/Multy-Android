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
import android.widget.TextView;

import java.util.ArrayList;

import io.multy.R;
import io.multy.ui.fragments.dialogs.ListDialogFragment;

public class CurrenciesAdapter extends RecyclerView.Adapter<CurrenciesAdapter.ViewHolder> {

    private ArrayList<String> data;
    private ListDialogFragment.OnCurrencyClickListener listener;

    public CurrenciesAdapter(ArrayList<String> data, ListDialogFragment.OnCurrencyClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.currency.setText(data.get(position));
        holder.currency.setOnClickListener(view -> listener.onClickCurrency(data.get(position), position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView currency;

        ViewHolder(View itemView) {
            super(itemView);
            currency = (TextView) itemView;
        }
    }
}
