/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.wallet.WalletInfoFragment;
import io.multy.viewmodels.AssetsViewModel;

/**
 * Created by Ihar Paliashchuk on 02.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class AssetsFragment extends BaseFragment {

    private AssetsViewModel viewModel;

    public static AssetsFragment newInstance(){
        return new AssetsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return DataBindingUtil.inflate(inflater, R.layout.fragment_wallet, container, false).getRoot();
        View view = inflater.inflate(R.layout.fragment_assets, container, false);
        viewModel = ViewModelProviders.of(this).get(AssetsViewModel.class);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.title)
    void onTitleCLick(){
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.full_container, WalletInfoFragment.newInstance())
                .commit();
    }
}
