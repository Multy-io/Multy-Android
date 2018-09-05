/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    private ArrayList<Owner> data = new ArrayList<>();
    private View.OnLongClickListener listener;

    public OwnersAdapter(View.OnLongClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_OWNER) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_waiting, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_OWNER) {
            final Owner owner = data.get(position);
            holder.image.setAvatar(owner.getAddress());
            holder.textAddress.setText(owner.getAddress());
            holder.root.setTag(data.get(position).getAddress());
            holder.root.setOnLongClickListener(listener);
//            holder.textName.setText(owner.getName());
        }

    }

    public void setOwners(ArrayList<Owner> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position) == null ? TYPE_WAITING : TYPE_OWNER;
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
        @BindView(R.id.root)
        View root;

        public ViewHolder(View itemView) {
            super(itemView);
            if (itemView.findViewById(R.id.image) instanceof Hash2PicView) {
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
