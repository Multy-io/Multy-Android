/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.ui.fragments.dialogs.PrivateKeyDialogFragment;

/**
 * Created by anschutz1927@gmail.com on 22.02.18.
 */

public class AssetSettingAddressesAdapter extends RecyclerView.Adapter<AssetSettingAddressesAdapter.Holder> {

    private final int leftMargin;
    private int currencyId;
    private int networkId;
    private boolean isItemSelected = false;
    private FragmentManager fragmentManager;
    private List<WalletAddress> addresses;

    public AssetSettingAddressesAdapter(FragmentManager childFragmentManager, DisplayMetrics displayMetrics) {
        this.fragmentManager = childFragmentManager;
        leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, displayMetrics);
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
            holder.textBalance.setText(Wallet.getAddressAmount(address, currencyId));
            holder.itemView.setOnClickListener(view -> {
                if (!isItemSelected) {
                    isItemSelected = true;
                    PrivateKeyDialogFragment dialog = PrivateKeyDialogFragment
                            .getInstance(address, currencyId, networkId);
                    dialog.show(fragmentManager, dialog.getTag());
                    dialog.setListener(() -> isItemSelected = false);
                }
            });
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.divider.getLayoutParams();
            params.leftMargin = position == getItemCount() - 1? 0 : leftMargin;
            holder.divider.setLayoutParams(params);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return addresses == null ? 0 : addresses.size();
    }

    public void setData(List<WalletAddress> addresses, int currencyId, int networkId) {
        this.addresses = addresses;
        this.currencyId = currencyId;
        this.networkId = networkId;
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_address)
        TextView textAddress;
        @BindView(R.id.text_balance_original)
        TextView textBalance;
        @BindView(R.id.divider)
        View divider;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
