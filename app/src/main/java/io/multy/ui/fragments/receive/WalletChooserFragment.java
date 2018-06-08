/*
 * Copyright 2018 Idealnaya rabota LLC
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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.adapters.MyWalletsAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetRequestViewModel;


public class WalletChooserFragment extends BaseFragment implements MyWalletsAdapter.OnWalletClickListener {

    public static WalletChooserFragment newInstance() {
        return new WalletChooserFragment();
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private AssetRequestViewModel viewModel;
    private MyWalletsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);
//        viewModel.setContext(getActivity()); //TODO review
        setBaseViewModel(viewModel);
        if (!getActivity().getIntent().hasExtra(Constants.EXTRA_WALLET_ID)) {
            Analytics.getInstance(getActivity()).logReceiveLaunch(viewModel.getChainId());
        }
    }

    @Override
    public void onStart() {
        if (getActivity() != null) {
            getActivity().getIntent().removeExtra(Constants.EXTRA_WALLET_ID);
        }
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_chooser, container, false);
        ButterKnife.bind(this, view);
        setupAdapter();
        return view;
    }

    @Override
    public void onWalletClick(Wallet wallet) {
        viewModel.setWallet(wallet);
        viewModel.setAddress(wallet.getActiveAddress().getAddress());
        Analytics.getInstance(getActivity()).logReceive(AnalyticsConstants.RECEIVE_WALLET_CLICK, viewModel.getChainId());
        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
            ((AssetRequestActivity) getActivity()).setFragment(R.string.receive, RequestSummaryFragment.newInstance());
        } else {
            getActivity().onBackPressed();
        }
    }

    private void setupAdapter() {
        if (adapter == null) {
            adapter = new MyWalletsAdapter(this, viewModel.getWalletsDB());
        }
        recyclerView.setAdapter(adapter);
    }

}