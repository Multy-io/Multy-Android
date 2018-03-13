/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.multy.R;
import io.multy.util.BrickView;

public class BricksAdapter extends RecyclerView.Adapter<BricksAdapter.ViewHolder> {

    private static final int COUNT = 15;

    private int brickBackgroundResId = -1;
    private boolean isGreenMode = false;

    public BricksAdapter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brick, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (brickBackgroundResId != -1) {
            holder.brickView.setBrickBackground(brickBackgroundResId);
        }

        if (isGreenMode) {
            holder.brickView.enableGreenMode();
        }
    }

    @Override
    public int getItemCount() {
        return COUNT;
    }

    public void setBrickBackgroundResId(int brickBackgroundResId) {
        this.brickBackgroundResId = brickBackgroundResId;
    }

    public void enableGreenMode() {
        isGreenMode = true;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        BrickView brickView;

        ViewHolder(View itemView) {
            super(itemView);
            brickView = (BrickView) itemView;
        }
    }
}
