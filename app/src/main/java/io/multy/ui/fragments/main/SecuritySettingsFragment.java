/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.ResetDataDialogFragment;

public class SecuritySettingsFragment extends BaseFragment {

    public static SecuritySettingsFragment newInstance() {
        return new SecuritySettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_security, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @OnClick(R.id.button_back)
    public void onClickBack() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @OnClick(R.id.container_restore_seed)
    public void onClickRestoreSeed() {

    }

    @OnClick(R.id.container_entrance_settings)
    public void onClickEntranceSettings() {

    }

    @OnClick(R.id.container_reset_data)
    public void onClickResetDada() {
        ResetDataDialogFragment resetDataDialog = new ResetDataDialogFragment();
        resetDataDialog.setCancelable(false);
        resetDataDialog.show(getFragmentManager(), null);
    }

}
