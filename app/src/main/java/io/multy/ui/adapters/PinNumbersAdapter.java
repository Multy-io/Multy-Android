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
import android.widget.TextView;

import io.multy.R;

public class PinNumbersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnFingerPrintClickListener {
        void onFingerprintClick();
    }

    public interface OnNumberClickListener {
        void onNumberClick(int number);
    }

    private static final int COUNT = 12;
    private static final int TYPE_NUMBER = 202;
    private static final int TYPE_IMAGE = 203;
    private static final int TYPE_EMPTY = 204;


    private OnNumberClickListener numberClickListener;
    private OnFingerPrintClickListener fingerPrintClickListener;
    private boolean isFingerprintAllowed = false;

    public PinNumbersAdapter(OnNumberClickListener numberClickListener, OnFingerPrintClickListener fingerPrintClickListener, boolean isFingerprintAllowed) {
        this.numberClickListener = numberClickListener;
        this.fingerPrintClickListener = fingerPrintClickListener;
        this.isFingerprintAllowed = isFingerprintAllowed;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_NUMBER:
                return new NumberHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pin_number, parent, false));
            case TYPE_IMAGE:
                return new ImageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pin_image, parent, false));
            default:
                return new EmptyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pin_empty, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_IMAGE:
                ((ImageHolder) holder).image.setOnClickListener(view -> fingerPrintClickListener.onFingerprintClick());
                break;
            case TYPE_NUMBER:
                NumberHolder numberHolder = (NumberHolder) holder;
                numberHolder.number.setText(position < 9 ? String.valueOf(position + 1) : String.valueOf(0));
                numberHolder.number.setOnClickListener(view -> numberClickListener.onNumberClick(Integer.valueOf(numberHolder.number.getText().toString())));
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 9 || position == 10) {
            return TYPE_NUMBER;
        } else if (position == 11 && isFingerprintAllowed) {
            return TYPE_IMAGE;
        } else {
            return TYPE_EMPTY;
        }
    }

    @Override
    public int getItemCount() {
        return COUNT;
    }

    public void setFingerprintAllowed(boolean fingerprintAllowed) {
        isFingerprintAllowed = fingerprintAllowed;
    }

    static class NumberHolder extends RecyclerView.ViewHolder {

        TextView number;

        NumberHolder(View itemView) {
            super(itemView);
            number = (TextView) itemView;
        }
    }

    static class ImageHolder extends RecyclerView.ViewHolder {

        ImageView image;

        ImageHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView;
        }
    }

    static class EmptyHolder extends RecyclerView.ViewHolder {

        EmptyHolder(View itemView) {
            super(itemView);
        }
    }
}
