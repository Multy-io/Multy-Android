/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.PinSetupActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;

public class EntranceSettingsFragment extends BaseFragment {

    public static EntranceSettingsFragment newInstance() {
        return new EntranceSettingsFragment();
    }

    @BindView(R.id.button_warn)
    ConstraintLayout buttonWarn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_entrance, container, false);
        ButterKnife.bind(this, view);

        buttonWarn.setVisibility(Prefs.getBoolean(Constants.PREF_BACKUP_SEED) ? View.GONE : View.VISIBLE);

        return view;
    }

    @OnClick(R.id.button_back)
    public void onClickBack() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @OnClick(R.id.container_setup_pin)
    void onClickPin(View view) {
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 1500);
        startActivity(new Intent(getActivity(), PinSetupActivity.class));
    }
}
