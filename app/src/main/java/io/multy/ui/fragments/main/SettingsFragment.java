/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.storage.SecurePreferencesHelper;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.SettingsViewModel;

public class SettingsFragment extends BaseFragment {

    private final static String TAG = SettingsFragment.class.getSimpleName();

    @BindView(R.id.check_lock)
    CheckBox checkBoxLock;

    @BindView(R.id.text_lock_mode)
    TextView textViewLock;

    @BindView(R.id.input_pin)
    EditText inputPin;

    @BindView(R.id.button_save_pin)
    Button buttonPin;

    @BindView(R.id.check_finger)
    CheckBox checkBoxFinger;

    @BindView(R.id.text_finger_mode)
    TextView textViewFinger;

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
        checkBoxLock.setChecked(lock);


        final boolean finger = Prefs.getBoolean(Constants.PREF_IS_FINGERPRINT_ENABLED);
        textViewLock.setText(lock ? "Enabled" : "Disabled");
        checkBoxLock.setChecked(finger);

        return view;
    }

    @OnClick(R.id.button_lock)
    public void onClickLock() {
        checkBoxLock.setChecked(!checkBoxLock.isChecked());
        Prefs.putBoolean(Constants.PREF_LOCK, checkBoxLock.isChecked());

        final boolean lock = checkBoxLock.isChecked();
        textViewLock.setText(lock ? "Enabled" : "Disabled");
    }

    @OnClick(R.id.button_fingerprint)
    public void onClickFinger() {
        checkBoxLock.setChecked(!checkBoxFinger.isChecked());
        Prefs.putBoolean(Constants.PREF_IS_FINGERPRINT_ENABLED, checkBoxLock.isChecked());

        final boolean finger = checkBoxLock.isChecked();
        textViewFinger.setText(finger ? "Enabled" : "Disabled");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.button_save_pin)
    public void onClickSavePin() {
        final String pin = inputPin.getText().toString();
        SecurePreferencesHelper.putString(getActivity(), Constants.PREF_PIN, pin);
    }

    @OnClick(R.id.button_backup)
    public void onClickBackup() {
        startActivity(new Intent(getActivity(), SeedActivity.class));
    }

}
