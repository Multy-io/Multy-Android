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
import android.widget.Toast;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.adapters.AddressesAdapter;
import io.multy.util.Constants;

public class AddressesFragment extends BaseFragment {

    public static AddressesFragment newInstance(int walletIndex) {
        AddressesFragment addressesFragment = new AddressesFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(Constants.EXTRA_WALLET_ID, walletIndex);
        addressesFragment.setArguments(arguments);
        return addressesFragment;
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindInt(R.integer.one_negative)
    int oneNegative;

//    private WalletAddressesAdapter adapter;
//    private WalletViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_addresses, container, false);
        ButterKnife.bind(this, convertView);

//        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
//        viewModel.setContext(getActivity());
//        viewModel.getUserAssets();
//        viewModel.getWalletAddresses(0);
//        viewModel.getAddresses().observe(this, addresses -> {
//            recyclerView.setAdapter(new AddressesAdapter(addresses));
//        });

//        initList();

        if (getActivity() instanceof AssetRequestActivity) {
            toolbar.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments().getInt(Constants.EXTRA_WALLET_ID) != oneNegative) {
            WalletRealmObject wallet = DataManager.getInstance().getWallet(getArguments().getInt(Constants.EXTRA_WALLET_ID, oneNegative));
            recyclerView.setAdapter(new AddressesAdapter(wallet.getAddresses()));
        } else {
            Toast.makeText(getActivity(), R.string.addresses_empty, Toast.LENGTH_SHORT).show();
        }
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
