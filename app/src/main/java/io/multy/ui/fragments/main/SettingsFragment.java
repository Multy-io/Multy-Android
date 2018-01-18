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
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.SettingsViewModel;

public class SettingsFragment extends BaseFragment {

    private final static String TAG = SettingsFragment.class.getSimpleName();

    @BindView(R.id.switch_push)
    Switch notificationsView;

    private SettingsViewModel viewModel;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        ButterKnife.bind(this, view);

//        final boolean lock = Prefs.getBoolean(Constants.PREF_LOCK);
//        notificationsView.setChecked(lock);

        return view;
    }

    @OnClick(R.id.container_push)
    public void onClickPush() {
//        final boolean lock = !notificationsView.isChecked();
//        Prefs.putBoolean(Constants.PREF_LOCK, lock);
//        notificationsView.setChecked(lock);
    }

    @OnClick(R.id.container_security)
    public void onClickSettings() {

    }

    @OnClick(R.id.container_about)
    public void onClickAbout() {

    }

    @OnClick(R.id.container_feedback)
    public void onClickFeedback() {

    }
}
