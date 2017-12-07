/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;


import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.Fee;
import timber.log.Timber;

public class FeeAdapter extends RecyclerView.Adapter<FeeAdapter.FeeHolder> {

    private int[] iconIds;
    private List<String> names;
    private List<String> blockIds;
    private List<Fee> wallets;
    private OnFeeClickListener listener;
    private int previousPosition;
    private Fee previousFee;
    private Fee savedFee;

    public FeeAdapter(Context context, OnFeeClickListener listener, Fee fee) {
        this.listener = listener;
        wallets = new ArrayList<>();
        iconIds = new int[]{R.drawable.ic_very_fast, R.drawable.ic_fast, R.drawable.ic_medium, R.drawable.ic_slow, R.drawable.ic_very_slow};
        names = Arrays.asList(context.getResources().getStringArray(R.array.fees));
        blockIds = Arrays.asList(context.getResources().getStringArray(R.array.blocks));
        for (double i = 0; i < iconIds.length; i++) { // TODO remove with real fees
            wallets.add(new Fee(names.get((int) i), "" + i/1000 + " BTC", i/1000, false));
        }
        previousPosition = -1;
        Timber.e("savedFee %s", fee);
        savedFee = fee;
    }

    @Override
    public FeeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fee, parent, false);
        return new FeeHolder(view);
    }

    @Override
    public void onBindViewHolder(FeeHolder holder, int position) {
        holder.bind(wallets.get(position));
    }

    @Override
    public int getItemCount() {
        return wallets.size();
    }


    public class FeeHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)
        ConstraintLayout root;
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

        public FeeHolder(View itemView) {
            super(itemView);
            setIsRecyclable(false);
            ButterKnife.bind(this, itemView);
        }

        void bind(final Fee fee) {
            textName.setText(names.get(getAdapterPosition()));
            textBalanceOriginal.setText(fee.getCost());
            imageLogo.setImageResource(iconIds[getAdapterPosition()]);
            textBlocks.setText(blockIds.get(getAdapterPosition()));
            if (getAdapterPosition() == getItemCount() - 1){
                divider.setVisibility(View.GONE);
//                textBalanceOriginal.setVisibility(View.GONE);
            }
            imageMark.setVisibility(fee.isSelected() ? View.VISIBLE : View.INVISIBLE);
            if (savedFee != null && savedFee.getTime().equals(fee.getTime())) {
                imageMark.setVisibility(View.VISIBLE);
                savedFee = null;
            }
            root.setOnClickListener(view -> {
                imageMark.setVisibility(fee.isSelected() ? View.VISIBLE : View.INVISIBLE);
                fee.setSelected(imageMark.getVisibility() == View.INVISIBLE);

                if (previousFee != null){
                    previousFee.setSelected(false);
                }

                if (previousPosition == getAdapterPosition()){
                    previousFee = null;
                    previousPosition = -1;
                } else {
                    previousFee = fee;
                    previousPosition = getAdapterPosition();
                }

                notifyDataSetChanged();

                listener.onFeeClick(fee);
            });
        }
    }

    public interface OnFeeClickListener {
        void onFeeClick(Fee fee);
    }
}
