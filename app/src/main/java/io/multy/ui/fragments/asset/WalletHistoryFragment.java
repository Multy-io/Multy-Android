/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;

public class WalletHistoryFragment extends BaseFragment {

    public static WalletHistoryFragment newInstance() {
        return new WalletHistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return DataBindingUtil.inflate(inflater, R.layout.fragment_wallet, container, false).getRoot();
        View view = inflater.inflate(R.layout.fragment_asset_history, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

}
