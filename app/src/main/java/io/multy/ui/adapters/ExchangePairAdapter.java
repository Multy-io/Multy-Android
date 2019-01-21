/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.ExchangeAsset;
import io.multy.util.Constants;
import io.realm.RealmResults;
import timber.log.Timber;

public class ExchangePairAdapter extends RecyclerView.Adapter<ExchangePairAdapter.Holder> {
    private List<ExchangeAsset> data;
    private ExchangePairAdapter.OnAssetClickListener listener;

    public ExchangePairAdapter (ExchangePairAdapter.OnAssetClickListener listener, List<ExchangeAsset> data) {
        this.listener = listener;
        this.data = data;
    }

    @NonNull
    @Override
    public ExchangePairAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExchangePairAdapter.Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exchange_asset, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExchangePairAdapter.Holder holder, int position) {
        ExchangeAsset asset = data.get(position);
        holder.itemView.setOnClickListener(view -> {
            for (ExchangeAsset toSetAsset : data){
                toSetAsset.setSelected(false);
            }
            asset.setSelected(true);
            if (listener != null) {
                listener.onAssetClick(asset);
            } else {
                Timber.e("Forgot to set listener?");
            }
        });

        holder.tvName.setText(asset.getName());
        holder.tvFullName.setText(asset.getFullName());
        if (asset.isSelected()){
            holder.ivSelected.setVisibility(View.VISIBLE);
        } else {
            holder.ivSelected.setVisibility(View.GONE);
        }

        if (asset.getLogo()!= null){
            if (asset.getName().toLowerCase().equals(Constants.BTC.toLowerCase())){
                int resId = Integer.parseInt(asset.getLogo());
                Picasso.get().load(resId).into(holder.ivLogo);
            } else if (asset.getName().toLowerCase().equals(Constants.ETH.toLowerCase())){
                int resId = Integer.parseInt(asset.getLogo());
                Picasso.get().load(resId).into(holder.ivLogo);
            } else {
                Picasso.get().load(asset.getLogo()).into(holder.ivLogo);
            }
        } else {
            Picasso.get().load(R.drawable.chain_erc_20).into(holder.ivLogo);
        }

    }

    @Override
    public int getItemCount() {
        return data == null || (data instanceof RealmResults && !((RealmResults) data).isValid()) ? 0 : data.size();
    }


    public void setData(List<ExchangeAsset> data) {
        this.data = data;
        notifyDataSetChanged();
        Log.d("EXCHANGE ADAPTER", "SET DATA CALLED: " + this.data.size());
    }

    public void setListener(ExchangePairAdapter.OnAssetClickListener listener) {
        this.listener = listener;
    }

    public List<ExchangeAsset> getData() {
        return data;
    }

    public ExchangeAsset getItem(int position) {
        return data.get(position);
    }

    class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_asset_short)
        TextView tvName;
        @BindView(R.id.tv_asset_long)
        TextView tvFullName;
        @BindView(R.id.iv_check)
        ImageView ivSelected;
        @BindView(R.id.image_logo)
        ImageView ivLogo;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnAssetClickListener {
        void onAssetClick(ExchangeAsset asset);
    }
}
