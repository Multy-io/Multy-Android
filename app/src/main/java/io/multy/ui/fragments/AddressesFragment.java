/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.adapters.WalletAddressesAdapter;
import io.multy.util.NativeDataHelper;

public class AddressesFragment extends BaseFragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private WalletAddressesAdapter adapter;
    private WalletRealmObject wallet;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_addresses, container, false);
        ButterKnife.bind(this, convertView);

        initList();
        return convertView;
    }

    private void initList() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        adapter = new WalletAddressesAdapter(wallet.getAddresses(), NativeDataHelper.Currency.values()[wallet.getCurrency()]);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    public void setWallet(WalletRealmObject wallet) {
        this.wallet = wallet;
    }

    @OnClick(R.id.text_cancel)
    public void onClickCancel() {
        getActivity().onBackPressed();
    }
}
