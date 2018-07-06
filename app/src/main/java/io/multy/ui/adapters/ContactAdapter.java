/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.net.Uri;
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
import io.multy.model.entities.Contact;
import io.multy.storage.RealmManager;
import io.realm.RealmResults;

/**
 * Created by anschutz1927@gmail.com on 21.06.18.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.Holder> {

    private final OnClickListener listener;
    private RealmResults<Contact> contacts;

    public ContactAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.textName.setText(contacts.get(position).getName());
        holder.textCounter.setText(String.valueOf(contacts.get(position).getAddresses().size()));
        if (contacts.get(position).getPhotoUri() != null) {
            holder.imagePhoto.setImageURI(Uri.parse(contacts.get(position).getPhotoUri()));
        } else {
            holder.imagePhoto.setImageURI(null);
        }
    }

    @Override
    public int getItemCount() {
        return contacts == null ? 0 : contacts.size();
    }

    public void notifyData() {
        contacts = RealmManager.getSettingsDao().getContacts();
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_photo)
        ImageView imagePhoto;
        @BindView(R.id.text_name)
        TextView textName;
        @BindView(R.id.text_counter)
        TextView textCounter;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> listener.onClickContact(contacts.get(getAdapterPosition()).getId()));
            itemView.setOnLongClickListener(v -> listener.onLongClickContact(contacts.get(getAdapterPosition()).getId()));
        }
    }

    public interface OnClickListener {
        void onClickContact(long contactId);
        boolean onLongClickContact(long contactId);
    }
}
