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
import io.multy.model.entities.ContactAddress;
import io.realm.RealmList;

/**
 * Created by anschutz1927@gmail.com on 27.06.18.
 */
public class ContactAddressesAdapter extends RecyclerView.Adapter<ContactAddressesAdapter.Holder> {

    private final OnClickListener listener;
    private RealmList<ContactAddress> addresses;

    public ContactAddressesAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_address, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.imageCurrency.setImageResource(addresses.get(position).getCurrencyImgId());
        holder.textAddress.setText(addresses.get(position).getAddress());
    }

    @Override
    public int getItemCount() {
        return addresses == null ? 0 : addresses.size();
    }

    public void setData(RealmList<ContactAddress> addresses) {
        this.addresses = addresses;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_currency)
        ImageView imageCurrency;
        @BindView(R.id.text_address)
        TextView textAddress;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(view -> listener.onClickAddress(addresses.get(getAdapterPosition())));
            itemView.setOnLongClickListener(view -> {
                ContactAddress address = addresses.get(getAdapterPosition());
                return listener.onLongClickAddress(address.getAddress(), address.getCurrencyId(), address.getNetworkId());
            });
        }
    }

    public interface OnClickListener {
        void onClickAddress(ContactAddress address);
        boolean onLongClickAddress(String address, int currencyId, int networkId);
    }
}
