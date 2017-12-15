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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.SettingsViewModel;

/**
 * Created by Ihar Paliashchuk on 02.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class SettingsFragment extends BaseFragment {


    private SettingsViewModel viewModel;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
//        return DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false).getRoot();
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.seed)
    public void onClickSeed() {
        startActivity(new Intent(getActivity(), SeedActivity.class));
    }

}
