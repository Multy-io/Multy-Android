/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.adapters.WalletAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.AssetSendViewModel;


public class WalletChooserFragment extends BaseFragment implements WalletAdapter.OnWalletClickListener {

    public static WalletChooserFragment newInstance() {
        return new WalletChooserFragment();
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private AssetSendViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_chooser, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
//        if (viewModel.getExchangePriceLive().getValue() != null) {
            setupAdapter(viewModel.getExchangePrice().getValue());
//        } else {
//            viewModel.getExchangePrice();
//            viewModel.getExchangePriceLive().observe(this, this::setupAdapter);
//        }
        return view;
    }

    @Override
    public void onWalletClick(WalletRealmObject wallet) {
        viewModel.setWallet(wallet);
        ((AssetSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, TransactionFeeFragment.newInstance());
    }

    private void setupAdapter(Double exchangePrice){
        WalletAdapter adapter = new WalletAdapter(exchangePrice, this);
        adapter.setWallets(viewModel.getWalletsDB());
        recyclerView.setAdapter(adapter);
    }

}