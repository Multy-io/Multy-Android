/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.activities.CreateAssetActivity;
import io.multy.ui.adapters.PortfoliosAdapter;
import io.multy.ui.adapters.WalletsAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.asset.AssetInfoFragment;
import io.multy.viewmodels.AssetsViewModel;

/**
 * Created by Ihar Paliashchuk on 02.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class AssetsFragment extends BaseFragment {

    public static final String TAG = AssetsFragment.class.getSimpleName();

    @BindView(R.id.recycler_wallets)
    RecyclerView recyclerWallets;

    private AssetsViewModel viewModel;
    private WalletsAdapter walletsAdapter;
    private PortfoliosAdapter portfoliosAdapter;

    public static AssetsFragment newInstance() {
        return new AssetsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<WalletRealmObject> wallets = new ArrayList<>();
        WalletRealmObject wallet = new DataManager(getActivity()).getWallet();
        if (wallet != null) {
            wallets.add(wallet);
        }
        walletsAdapter = new WalletsAdapter(wallets);
        walletsAdapter = new WalletsAdapter(new ArrayList<>());
        portfoliosAdapter = new PortfoliosAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assets, container, false);
        subscribeViewModel();
        ButterKnife.bind(this, view);
        initialize();
        return view;
    }

    @Override
    public void onPause() {
        WalletActionsDialog dialog = (WalletActionsDialog) getChildFragmentManager()
                .findFragmentByTag(WalletActionsDialog.TAG);
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onPause();
    }

    private void initialize() {
        setupViewPager();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        recyclerWallets.setLayoutManager(layoutManager);
        recyclerWallets.setNestedScrollingEnabled(false);
        setAdapter();
    }

    private void subscribeViewModel() {
        viewModel = ViewModelProviders.of(this).get(AssetsViewModel.class);
    }

    private void setAdapter() {
        recyclerWallets.setAdapter(walletsAdapter);
    }

    private void setupViewPager() {
//        pagerPortfolios.setAdapter(portfoliosAdapter);
//        pagerPortfolios.setPageMargin(40);
        setVisibilityToPortfolios(false);
    }

    private void onItemClick() {

    }

    private void onClickPortfolio() {

    }

    private void onClickAsset() {

    }

    private void showAddWalletActions() {
        WalletActionsDialog.Callback callback = new WalletActionsDialog.Callback() {
            @Override
            public void onCardAddClick() {
                onWalletAddClick();
            }

            @Override
            public void onCardImportClick() {
                onWalletImportClick();
            }
        };
        WalletActionsDialog dialog = (WalletActionsDialog) getChildFragmentManager()
                .findFragmentByTag(WalletActionsDialog.TAG);
        if (dialog == null) {
            dialog = WalletActionsDialog.newInstance(callback);
        }
        dialog.show(getChildFragmentManager(), WalletActionsDialog.TAG);
    }

    private void onWalletImportClick() {

    }

    private void onWalletAddClick() {
        startActivity(new Intent(getContext(), CreateAssetActivity.class));
    }

    private void setVisibilityToPortfolios(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
//        pagerPortfolios.setVisibility(visibility);
    }

    @OnClick(R.id.button_add)
    void onPlusClick() {
        showAddWalletActions();
    }

    @OnClick(R.id.title)
    void onTitleCLick() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.full_container, AssetInfoFragment.newInstance())
                .commit();
    }
}
