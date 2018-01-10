/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.adapters.AddressesAdapter;
import io.multy.util.Constants;

public class AddressesFragment extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.text_title)
    TextView textViewTitle;

    @BindInt(R.integer.one_negative)
    int oneNegative;

    public static AddressesFragment newInstance(int walletIndex) {
        AddressesFragment addressesFragment = new AddressesFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(Constants.EXTRA_WALLET_ID, walletIndex);
        addressesFragment.setArguments(arguments);
        return addressesFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_addresses, container, false);
        ButterKnife.bind(this, convertView);


        if (getActivity() instanceof AssetRequestActivity) {
            toolbar.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments().getInt(Constants.EXTRA_WALLET_ID) != oneNegative) {
            WalletRealmObject wallet = RealmManager.getAssetsDao().getWalletById(getArguments().getInt(Constants.EXTRA_WALLET_ID, oneNegative));
            textViewTitle.setText(wallet.getName());
            recyclerView.setAdapter(new AddressesAdapter(wallet.getAddresses()));
        } else {
            Toast.makeText(getActivity(), R.string.addresses_empty, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.text_cancel)
    public void onClickCancel() {
        getActivity().onBackPressed();
    }
}
