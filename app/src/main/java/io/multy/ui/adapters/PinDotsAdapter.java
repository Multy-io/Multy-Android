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
import android.widget.ImageView;

import io.multy.R;

public class PinDotsAdapter extends RecyclerView.Adapter<PinDotsAdapter.ViewHolder> {

    private static final int COUNT = 6;


    public PinDotsAdapter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pin_dot, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return COUNT;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView;
        }
    }
}
