/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.receive;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.adapters.MyWalletsAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetRequestViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class WalletChooserFragment extends BaseFragment implements MyWalletsAdapter.OnWalletClickListener {

    public static WalletChooserFragment newInstance() {
        return new WalletChooserFragment();
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeLayout;

    private AssetRequestViewModel viewModel;
    private MyWalletsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);
        setBaseViewModel(viewModel);
        if (!getActivity().getIntent().hasExtra(Constants.EXTRA_WALLET_ID)) {
            Analytics.getInstance(getActivity()).logReceiveLaunch(viewModel.getChainId());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_chooser, container, false);
        ButterKnife.bind(this, view);
        updateWallets();
        swipeLayout.setOnRefreshListener(this::updateWallets);
        return view;
    }

    @Override
    public void onStart() {
        if (getActivity() != null) {
            getActivity().getIntent().removeExtra(Constants.EXTRA_WALLET_ID);
        }
        super.onStart();
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

    private void updateWallets() {
        swipeLayout.setRefreshing(true);
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletsResponse> call, @NonNull Response<WalletsResponse> response) {
                if (response.body() != null) {
                    response.body().saveBtcTopWalletIndex();
                    response.body().saveEthTopWalletIndex();
                    if (response.body().getWallets() != null && response.body().getWallets().size() != 0) {
                        final long selectedWalletId = viewModel.getWallet().getId();
                        RealmManager.getAssetsDao().deleteAll();
                        RealmManager.getAssetsDao().saveWallets(response.body().getWallets());
                        viewModel.getWallet(selectedWalletId);
                    }
                }
                setupAdapter();
            }

            @Override
            public void onFailure(@NonNull Call<WalletsResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                setupAdapter();
            }
        });
    }

    private void setupAdapter() {
        swipeLayout.setRefreshing(false);
        if (adapter == null) {
            adapter = new MyWalletsAdapter(this, viewModel.getWalletsDB());
        } else {
            adapter.setData(viewModel.getWalletsDB());
        }
        recyclerView.setAdapter(adapter);
    }

}