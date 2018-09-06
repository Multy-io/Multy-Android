/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.Contact;
import io.multy.model.entities.TransactionOwner;
import io.multy.storage.RealmManager;
import io.multy.ui.Hash2PicView;
import io.multy.util.Constants;
import io.multy.util.DateHelper;

/**
 * Created by anschutz1927@gmail.com on 10.07.18.
 */
public class MultisigOwnersAdapter extends RecyclerView.Adapter<MultisigOwnersAdapter.Holder> {

    private ArrayList<TransactionOwner> owners;

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multisig_owner, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Contact contact = RealmManager.getSettingsDao().getContactOrNull(owners.get(position).getAddress());
        if (contact != null) {
            if (!TextUtils.isEmpty(contact.getPhotoUri())) {
                holder.imagePhoto.setImageURI(Uri.parse(contact.getPhotoUri()));
            } else {
                holder.imagePhoto.setAvatar(owners.get(position).getAddress());
            }
            holder.textName.setText(contact.getName());
        } else {
            holder.imagePhoto.setAvatar(owners.get(position).getAddress());
        }
        holder.textAddress.setText(owners.get(position).getAddress());
        if (owners.get(position).getConfirmationStatus() == Constants.MULTISIG_OWNER_STATUS_CONFIRMED) {
            holder.imageChecker.setImageResource(R.drawable.ic_member_status_confirmed);
            holder.textStatus.setText(holder.textStatus.getContext().getString(R.string.confirmed));
            holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_light));
            holder.textDate.setText(getActionDate(owners.get(position).getConfirmationTime()));
        } else if (owners.get(position).getConfirmationStatus() == Constants.MULTISIG_OWNER_STATUS_SEEN) {
            holder.imageChecker.setImageResource(R.drawable.ic_member_status_viewed);
            holder.textStatus.setText(holder.textStatus.getContext().getString(R.string.viewed));
            holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.blue_light));
            holder.textDate.setText(getActionDate(owners.get(position).getSeenTime()));
        } else if (owners.get(position).getConfirmationStatus() == Constants.MULTISIG_OWNER_STATUS_DECLINED) {
            holder.imageChecker.setImageResource(R.drawable.ic_member_status_declined);
            holder.textStatus.setText(holder.textStatus.getContext().getString(R.string.declined));
            holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red_warn));
            holder.textDate.setText(getActionDate(owners.get(position).getSeenTime()));
        } else {
            holder.textDate.setText(holder.textDate.getContext().getString(R.string.waiting_confirmation));
            holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.blue_light));
        }
    }

    @Override
    public int getItemCount() {
        return owners == null ? 0 : owners.size();
    }

    private String getActionDate(long serverTime) {
        return DateHelper.DATE_FORMAT_TRANSACTION_INFO.format(serverTime * 1000);
    }

    public void setOwners(ArrayList<TransactionOwner> owners) {
        this.owners = owners;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_photo)
        Hash2PicView imagePhoto;
        @BindView(R.id.image_checker)
        ImageView imageChecker;
        @BindView(R.id.text_address)
        TextView textAddress;
        @BindView(R.id.text_status)
        TextView textStatus;
        @BindView(R.id.text_name)
        TextView textName;
        @BindView(R.id.text_date)
        TextView textDate;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
