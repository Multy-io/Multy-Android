/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.adapters.AddressesAdapter;
import io.multy.viewmodels.WalletViewModel;

public class AddressesFragment extends BaseFragment {

    public static AddressesFragment newInstance() {
        return new AddressesFragment();
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

//    private WalletAddressesAdapter adapter;
    private WalletViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_addresses, container, false);
        ButterKnife.bind(this, convertView);

        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        viewModel.setContext(getActivity());
        viewModel.getUserAssets();
        viewModel.getAddresses().observe(this, addresses -> {
            recyclerView.setAdapter(new AddressesAdapter(addresses));
        });
        viewModel.getWalletAddresses(0);

//        initList();
        return convertView;
    }

//    private void initList() {
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        adapter = new WalletAddressesAdapter(wallet.getAddresses(), NativeDataHelper.Currency.values()[wallet.getCurrencyId()]);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setAdapter(adapter);
//    }

//    public void setWalletLive(WalletRealmObject wallet) {
//        this.wallet = wallet;
//    }

    @OnClick(R.id.text_cancel)
    public void onClickCancel() {
        getActivity().onBackPressed();
    }
}
