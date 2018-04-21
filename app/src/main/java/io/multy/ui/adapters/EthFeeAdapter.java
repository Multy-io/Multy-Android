/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class EthFeeAdapter extends RecyclerView.Adapter<EthFeeAdapter.Holder> {

    private OnItemClickListener listener;
    private String gasPrice;
    private String gasLimit;
    private int checkedPosition = -1;

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new Holder(inflater.inflate(R.layout.item_fee, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (position == getItemCount() - 1) {
            holder.textName.setText("Custom");
            holder.divider.setVisibility(GONE);
            holder.imageLogo.setImageResource(R.drawable.ic_custom);
            holder.textBalanceOriginal.setText(gasPrice == null || gasLimit == null ?
                    "" : "GAS Prise - " + gasPrice + "\n" + "GAS Limit - " + gasLimit);
            holder.itemView.setOnClickListener(v -> listener.onClickCustom());
        } else {
            //todo bind fee rates
        }
        if (checkedPosition == position) {
            holder.imageMark.setVisibility(VISIBLE);
        } else {
            holder.imageMark.setVisibility(GONE);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData() {
        notifyDataSetChanged();
    }

    public void setCustomFee(String gasPrice, String gasLimit) {
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        checkedPosition = getItemCount() - 1;
        notifyItemChanged(getItemCount() - 1);
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_logo)
        ImageView imageLogo;
        @BindView(R.id.textName)
        TextView textName;
        @BindView(R.id.text_balance_original)
        TextView textBalanceOriginal;
        @BindView(R.id.text_blocks)
        TextView textBlocks;
        @BindView(R.id.divider)
        View divider;
        @BindView(R.id.image_mark)
        ImageView imageMark;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnItemClickListener {
        void onClickCustom();
        void onClickFee(int position);
    }
}
