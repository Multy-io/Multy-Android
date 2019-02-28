/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.exchange;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.ExchangeActivity;
import io.multy.ui.adapters.MyWalletsAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.viewmodels.ExchangeViewModel;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;


public class ExchangeWalletChooserFragment extends BaseFragment implements MyWalletsAdapter.OnWalletClickListener {

    public static final int NO_VALUE = -1;

    private static final String ARG_BLOCKCHAIN_ID = "blockchainId";
    private static final String ARG_NETWORK_ID = "networkId";

    public static io.multy.ui.fragments.exchange.ExchangeWalletChooserFragment newInstance(int blockchainId, int networkId) {
        io.multy.ui.fragments.exchange.ExchangeWalletChooserFragment fragment = new io.multy.ui.fragments.exchange.ExchangeWalletChooserFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BLOCKCHAIN_ID, blockchainId);
        args.putInt(ARG_NETWORK_ID, networkId);
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    //TODO makeAnotherViewModel
    private ExchangeViewModel viewModel;
    int blockchainId;
    int networkId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            blockchainId = getArguments().getInt(ARG_BLOCKCHAIN_ID, NO_VALUE);
            networkId = getArguments().getInt(ARG_NETWORK_ID, NO_VALUE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_chooser, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(ExchangeViewModel.class);
        setupAdapter();
        if (getActivity() != null && !getActivity().getIntent().hasExtra(Constants.EXTRA_WALLET_ID)) {
            Analytics.getInstance(getActivity()).logSendFromLaunch();
        }
        return view;
    }

    @Override
    public void onWalletClick(Wallet wallet) {
        viewModel.setReceiveToWallet(wallet);
//        getActivity().getSupportFragmentManager().popBackStack(ExchangeFragment.TAG_SEND_SUCCESS,FragmentManager.POP_BACK_STACK_INCLUSIVE);
        ((ExchangeActivity) getActivity()).setFragment(R.string.exchanging, R.id.container, ExchangeFragment.newInstance());

    }

    private void setupAdapter() {
        RealmResults<Wallet> wallets = getActualWallets();
        MyWalletsAdapter adapter = new MyWalletsAdapter(this, wallets);
        recyclerView.setAdapter(adapter);
        RealmChangeListener<RealmResults<Wallet>> listener = adapter::setData;
        final LifecycleObserver observer = new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void onResume() {
                wallets.addChangeListener(listener);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            public void onPause() {
                if (wallets != null && wallets.isValid()) {
                    wallets.removeAllChangeListeners();
                }
            }
        };
        getLifecycle().addObserver(observer);
    }

    private RealmResults<Wallet> getActualWallets() {
        RealmResults<Wallet> wallets;
        if (blockchainId == NO_VALUE || networkId == NO_VALUE) {
            wallets = RealmManager.getAssetsDao().getWallets();
        } else if (blockchainId == NativeDataHelper.Blockchain.ETH.getValue()) {
            wallets = RealmManager.getAssetsDao().getWallets(blockchainId, networkId, false);
        } else {
            wallets = RealmManager.getAssetsDao().getWallets(blockchainId, networkId, false);
        }
        return wallets;
    }
}