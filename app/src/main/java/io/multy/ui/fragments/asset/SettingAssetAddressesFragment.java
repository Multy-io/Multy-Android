/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.adapters.AssetSettingAddressesAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.WalletViewModel;

/**
 * Created by anschutz1927@gmail.com on 22.02.18.
 */

public class SettingAssetAddressesFragment extends BaseFragment {

    public static final String TAG = SettingAssetAddressesFragment.class.getSimpleName();

    @BindView(R.id.recycler_addresses)
    RecyclerView recyclerAddresses;

    private AssetSettingAddressesAdapter addressesAdapter;
    private WalletViewModel viewModel;

    public static SettingAssetAddressesFragment getInstance() {
        return new SettingAssetAddressesFragment();
    }

    public SettingAssetAddressesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_asset_setting_addresses, container, false);
        ButterKnife.bind(this, v);
        init();
        return v;
    }

    private void init() {
        addressesAdapter = new AssetSettingAddressesAdapter(getChildFragmentManager());
        recyclerAddresses.setAdapter(addressesAdapter);
        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        viewModel.getWalletLive().observe(this, walletRealmObject -> {
            if (walletRealmObject == null || walletRealmObject.getAddresses() == null) {
                return;
            }
            addressesAdapter.setData(walletRealmObject.getAddresses());
        });
    }

    @Override
    public void onDestroyView() {
        viewModel.getWalletLive().removeObservers(this);
        super.onDestroyView();
    }

    @OnClick(R.id.button_back)
    void onBackArrowClick(View v) {
        if (getActivity() != null) {
            v.setEnabled(false);
            getActivity().onBackPressed();
        }
    }
}
