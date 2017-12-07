/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.adapters.AssetTransactionsAdapter;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.WalletViewModel;

public class AssetInfoFragment extends BaseFragment {

    public static final String TAG = AssetInfoFragment.class.getSimpleName();

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_transactions)
    RecyclerView recyclerView;
    @BindView(R.id.constraint_empty)
    ConstraintLayout emptyAsset;
    @BindView(R.id.text_value)
    TextView textBalanceOriginal;
    @BindView(R.id.text_amount)
    TextView textBalanceFiat;
    @BindView(R.id.text_address)
    TextView textAddress;

    private WalletViewModel viewModel;

    private AssetTransactionsAdapter transactionsAdapter;

    public static AssetInfoFragment newInstance() {
        return new AssetInfoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionsAdapter = new AssetTransactionsAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return DataBindingUtil.inflate(inflater, R.layout.fragment_wallet, container, false).getRoot();
        View view = inflater.inflate(R.layout.fragment_asset_info, container, false);
        ButterKnife.bind(this, view);

        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
//        viewModel.getWalletLive().observe(this, walletRealmObject -> {
//            textAddress.setText(walletRealmObject.getAddresses().get(walletRealmObject.getAddresses().size() - 1).getAddress());
//            textBalanceOriginal.setText(String.valueOf(walletRealmObject.getCurrency()));
//            textBalanceFiat.setText(String.valueOf(walletRealmObject.getFiatCurrency()));
//        });

        textAddress.setText(viewModel.getWallet().getCreationAddress());
        textBalanceOriginal.setText(String.valueOf(viewModel.getWallet().getBalance()));
        textBalanceFiat.setText(String.valueOf(viewModel.getWallet().getFiatCurrency()));

        initialize();
        return view;
    }

    private void initialize() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(transactionsAdapter);
        if (transactionsAdapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyAsset.setVisibility(View.VISIBLE);
            setToolbarScrollFlag(0);
        }
        else {
            emptyAsset.setVisibility(View.GONE);
            setToolbarScrollFlag(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL);
        }
    }

    private void setToolbarScrollFlag(int flag) {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(flag);
    }

    private void copyAddress() {

    }

    private void switchNfcPayment() {

    }

    private void subscribeViewModel() {

    }

    @OnClick(R.id.card_addresses)
    void onClickAddress() {
        ((AssetActivity) getActivity()).setFragment(R.id.container_full, AddressesFragment.newInstance());
    }

    @OnClick(R.id.close)
    void onCloseClick() {
        getActivity().finish();
    }
}
