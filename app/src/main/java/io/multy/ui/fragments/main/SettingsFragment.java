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
import android.widget.CheckBox;
import android.widget.TextView;

import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.SettingsViewModel;

public class SettingsFragment extends BaseFragment {

    @BindView(R.id.check_lock)
    CheckBox checkBox;

    @BindView(R.id.text_lock_mode)
    TextView textViewLock;

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

        final boolean lock = Prefs.getBoolean(Constants.PREF_LOCK);
        textViewLock.setText(lock ? "Enabled" : "Disabled");
        checkBox.setChecked(lock);

        return view;
    }

    @OnClick(R.id.button_lock)
    public void onClickLock() {
        checkBox.setChecked(!checkBox.isChecked());
        Prefs.putBoolean(Constants.PREF_LOCK, checkBox.isChecked());

        final boolean lock = checkBox.isChecked();
        textViewLock.setText(lock ? "Enabled" : "Disabled");
    }

    @OnClick(R.id.button_backup)
    public void onClickBackup() {
        startActivity(new Intent(getActivity(), SeedActivity.class));
    }

}
