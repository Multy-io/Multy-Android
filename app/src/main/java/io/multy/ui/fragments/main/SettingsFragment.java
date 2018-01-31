/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.google.firebase.messaging.FirebaseMessaging;
import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.UserId;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.BaseActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.SettingsViewModel;

public class SettingsFragment extends BaseFragment implements BaseActivity.OnLockCloseListener {

    private final static String TAG = SettingsFragment.class.getSimpleName();

    @BindView(R.id.switch_push)
    SwitchCompat notificationsView;

    private SettingsViewModel viewModel;
    private boolean isSettingsClicked;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        ButterKnife.bind(this, view);
        isSettingsClicked = false;
        setOnCheckedChangeListener();

        if (Prefs.getBoolean(Constants.PREF_IS_PUSH_ENABLED, true)) {
            notificationsView.setChecked(true);
        } else {
            notificationsView.setChecked(false);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity)getActivity()).setOnLockCLoseListener(this);
    }

    @Override
    public void onStop() {
        ((BaseActivity)getActivity()).setOnLockCLoseListener(null);
        super.onStop();
    }

    @OnClick(R.id.container_security)
    public void onClickSettings() {
        if (Prefs.contains(Constants.PREF_LOCK) && Prefs.getBoolean(Constants.PREF_LOCK)) {
            isSettingsClicked = true;
            ((BaseActivity)getActivity()).showLock();
        } else {
            showSecuritySettingsFragment();
        }
    }

    @OnClick(R.id.container_about)
    public void onClickAbout() {

    }

    @OnClick(R.id.container_feedback)
    public void onClickFeedback() {

    }

    private void setOnCheckedChangeListener() {
        notificationsView.setOnCheckedChangeListener((compoundButton, checked) -> {
            UserId userId = RealmManager.getSettingsDao().getUserId();
            if (userId != null) {
                if (checked) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Constants.PUSH_TOPIC + userId.getUserId());
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.PUSH_TOPIC + userId.getUserId());
                }
                Prefs.putBoolean(Constants.PREF_IS_PUSH_ENABLED, checked);
            }
        });
    }

    @Override
    public void onLockClosed() {
        if (isSettingsClicked) {
            showSecuritySettingsFragment();
        }
    }

    private void showSecuritySettingsFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_container, SecuritySettingsFragment.newInstance())
                    .addToBackStack(SecuritySettingsFragment.class.getSimpleName())
                    .commit();
        }
    }
}
