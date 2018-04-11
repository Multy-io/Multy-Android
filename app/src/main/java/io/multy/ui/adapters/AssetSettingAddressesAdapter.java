/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.ui.fragments.dialogs.PrivateKeyDialogFragment;
import io.multy.util.CryptoFormatUtils;
import io.realm.RealmList;

/**
 * Created by anschutz1927@gmail.com on 22.02.18.
 */

public class AssetSettingAddressesAdapter extends RecyclerView.Adapter<AssetSettingAddressesAdapter.Holder> {

    private boolean isItemSelected = false;
    private FragmentManager fragmentManager;
    private RealmList<WalletAddress> addresses;

    public AssetSettingAddressesAdapter(FragmentManager childFragmentManager) {
        this.fragmentManager = childFragmentManager;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asset_setting_address, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        try {
            WalletAddress address = addresses.get(position);
            holder.textAddress.setText(address.getAddress());
            holder.textBalance.setText(String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(address.getAmount())));
            holder.itemView.setOnClickListener(view -> {
                if (!isItemSelected) {
                    isItemSelected = true;
                    PrivateKeyDialogFragment dialog = PrivateKeyDialogFragment.getInstance(address);
                    dialog.show(fragmentManager, dialog.getTag());
                    dialog.setListener(() -> isItemSelected = false);
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (addresses == null) {
            return 0;
        }
        return addresses.size();
    }

    public void setData(RealmList<WalletAddress> addresses) {
        this.addresses = addresses;
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_address)
        TextView textAddress;
        @BindView(R.id.text_balance_original)
        TextView textBalance;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
