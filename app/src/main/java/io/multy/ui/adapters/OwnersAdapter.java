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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.Owner;
import io.multy.ui.Hash2PicView;
import io.realm.RealmList;

public class OwnersAdapter extends RecyclerView.Adapter<OwnersAdapter.ViewHolder> {

    private static final int TYPE_WAITING = 101;
    private static final int TYPE_OWNER = 102;

    private RealmList<Owner> data;

    public OwnersAdapter(RealmList<Owner> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_OWNER) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_transaction_item_blocked, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_transaction_item_blocked, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_OWNER) {
            final Owner owner = data.get(position);
            holder.image.setAvatar(owner.getAddress());
            holder.textAddress.setText(owner.getAddress());
//            holder.textName.setText(owner.getName());
        }

    }

    public void setOwners(RealmList<Owner> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position > data.size() - 1 ? TYPE_WAITING : TYPE_OWNER;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image)
        Hash2PicView image;
        @BindView(R.id.text_name)
        TextView textName;
        @BindView(R.id.text_address)
        TextView textAddress;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
