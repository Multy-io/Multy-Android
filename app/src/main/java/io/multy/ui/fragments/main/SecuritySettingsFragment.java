/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.ResetDataDialogFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;

public class SecuritySettingsFragment extends BaseFragment {

    public static SecuritySettingsFragment newInstance() {
        return new SecuritySettingsFragment();
    }

    @BindView(R.id.image_warning)
    ImageView imageWarning;
    @BindView(R.id.text_state_entrance)
    TextView textStateEntrance;
    @BindView(R.id.text_restore_seed)
    TextView textRestoreSeed;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_security, container, false);
        ButterKnife.bind(this, view);
        Analytics.getInstance(getActivity()).logSecuritySettingsLaunch();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUI();
    }

    @OnClick(R.id.button_back)
    public void onClickBack() {
        Analytics.getInstance(getActivity()).logSecuritySettings(AnalyticsConstants.BUTTON_CLOSE);
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @OnClick(R.id.container_restore_seed)
    public void onClickRestoreSeed() {
        Analytics.getInstance(getActivity()).logSecuritySettings(AnalyticsConstants.SECURITY_SETTINGS_VIEW_SEED);
        if (Prefs.getBoolean(Constants.PREF_BACKUP_SEED)) {
            startActivity(new Intent(getActivity(), SeedActivity.class).setType(Constants.FLAG_VIEW_SEED_PHRASE));
        } else {
            startActivity(new Intent(getActivity(), SeedActivity.class));
        }
    }

    @OnClick(R.id.container_entrance_settings)
    public void onClickEntranceSettings() {
        Analytics.getInstance(getActivity()).logSecuritySettings(AnalyticsConstants.SECURITY_SETTINGS_ENTRANCE);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_container, EntranceSettingsFragment.newInstance())
                    .addToBackStack(EntranceSettingsFragment.class.getSimpleName())
                    .commit();
        }
    }

    @OnClick(R.id.container_reset_data)
    public void onClickResetDada() {
        Analytics.getInstance(getActivity()).logSecuritySettings(AnalyticsConstants.SECURITY_SETTINGS_RESET);
        ResetDataDialogFragment resetDataDialog = new ResetDataDialogFragment();
        resetDataDialog.setCancelable(false);
        resetDataDialog.show(getFragmentManager(), null);
    }

    private void setUI() {
        if (Prefs.contains(Constants.PREF_PIN)) {
            imageWarning.setVisibility(View.GONE);
            textStateEntrance.setVisibility(View.GONE);
        }

        if (Prefs.getBoolean(Constants.PREF_BACKUP_SEED)) {
            textRestoreSeed.setText(R.string.view_seed);
        } else {
            textRestoreSeed.setText(R.string.backup_seed);
        }
    }

}
