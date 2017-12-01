/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.asset;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.adapters.AssetTransactionsAdapter;
import io.multy.ui.fragments.BaseFragment;

public class AssetInfoFragment extends BaseFragment {

    public static final String TAG = AssetInfoFragment.class.getSimpleName();

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_transactions)
    RecyclerView recyclerView;
    @BindView(R.id.constraint_empty)
    ConstraintLayout emptyAsset;

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

    @OnClick(R.id.close)
    void onCloseClick() {
        getActivity().finish();
    }
}
