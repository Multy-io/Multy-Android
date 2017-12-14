/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.receive;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.adapters.WalletAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.AssetRequestViewModel;
import timber.log.Timber;


public class WalletChooserFragment extends BaseFragment implements WalletAdapter.OnWalletClickListener {

    public static WalletChooserFragment newInstance() {
        return new WalletChooserFragment();
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindInt(R.integer.zero)
    int zero;

    private AssetRequestViewModel viewModel;
    private WalletAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);
        viewModel.setContext(getActivity());
        setBaseViewModel(viewModel);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_chooser, container, false);
        ButterKnife.bind(this, view);
        setupAdapter(viewModel.getExchangePrice());
        viewModel.getExchangePriceLive().observe(this, this::setupAdapter);
        return view;
    }

    @Override
    public void onWalletClick(WalletRealmObject wallet) {
        viewModel.setWallet(wallet);
        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == zero) {
            ((AssetRequestActivity) getActivity()).setFragment(R.string.receive_summary, RequestSummaryFragment.newInstance());
        } else {
            getActivity().onBackPressed();
        }
    }

    private void setupAdapter(Double exchangePrice){
        if (adapter == null) {
            adapter = new WalletAdapter(exchangePrice, WalletChooserFragment.this);
        }
        adapter.setWallets(viewModel.getWalletsDB());
        adapter.setExchangePrice(exchangePrice);
        recyclerView.setAdapter(adapter);

    }

}