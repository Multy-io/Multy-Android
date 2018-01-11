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

import io.multy.R;

public class EmptyTransactionsAdapter extends RecyclerView.Adapter<EmptyTransactionsAdapter.Holder> {

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_empty_transaction_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 4;
    }

    class Holder extends RecyclerView.ViewHolder {

        Holder(View itemView) {
            super(itemView);
        }
    }
}
