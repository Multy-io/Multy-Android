/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.BuildConfig;
import io.multy.R;
import io.multy.model.entities.UserId;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.BaseActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.asset.ChainChooserFragment;
import io.multy.ui.fragments.asset.CurrencyChooserFragment;
import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.SettingsViewModel;

public class SettingsFragment extends BaseFragment implements BaseActivity.OnLockCloseListener {

    private final static String TAG = SettingsFragment.class.getSimpleName();

    @BindView(R.id.switch_push)
    SwitchCompat notificationsView;
    @BindView(R.id.text_version_title)
    TextView textVersionTitle;
    @BindView(R.id.text_version_description)
    TextView textVersionDescription;

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
        Analytics.getInstance(getActivity()).logSettingsLaunch();

        if (Prefs.getBoolean(Constants.PREF_IS_PUSH_ENABLED, true)) {
            notificationsView.setChecked(true);
        } else {
            notificationsView.setChecked(false);
        }
        setOnCheckedChangeListener();
        printApplicationVersion();
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

    @Override
    public void onLockClosed() {
        if (isSettingsClicked) {
            showSecuritySettingsFragment();
        }
    }

    private void setOnCheckedChangeListener() {
        notificationsView.setOnCheckedChangeListener((compoundButton, checked) -> {
            UserId userId = RealmManager.getSettingsDao().getUserId();
            if (userId != null) {
                if (checked) {
                    Analytics.getInstance(getActivity()).logSettings(AnalyticsConstants.SETTINGS_PUSH_ENABLE);
                    FirebaseMessaging.getInstance().subscribeToTopic(Constants.PUSH_TOPIC + userId.getUserId());
                } else {
                    Analytics.getInstance(getActivity()).logSettings(AnalyticsConstants.SETTINGS_PUSH_DISABLE);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.PUSH_TOPIC + userId.getUserId());
                }
                Prefs.putBoolean(Constants.PREF_IS_PUSH_ENABLED, checked);
            }
        });
    }

    private void printApplicationVersion() {
        String gitVersion = getString(R.string.git_new_line).concat(Constants.SPACE);
        gitVersion = gitVersion.concat(BuildConfig.BUILD_COMMIT_DESCRIPTION).concat(Constants.NEW_LINE).concat(Constants.SPACE);
        gitVersion = gitVersion.concat(BuildConfig.BUILD_COMMIT_BRANCH).concat(Constants.NEW_LINE).concat(Constants.SPACE);
        gitVersion = gitVersion.concat(BuildConfig.BUILD_COMMIT_HASH).concat(Constants.NEW_LINE);
        String libVersion = getString(R.string.core_new_line).concat(Constants.SPACE);
        try {
            libVersion = libVersion.concat(NativeDataHelper.getLibraryVersion()).concat(Constants.NEW_LINE);
        } catch (JniException e) {
            e.printStackTrace();
        }
        String environment = getString(R.string.environment_new_line).concat(Constants.SPACE).concat(Constants.BASE_URL);
        String complexVersion = gitVersion.concat(libVersion).concat(environment);
        textVersionTitle.append(BuildConfig.VERSION_NAME);
        textVersionDescription.setText(complexVersion);
    }

    private void showSecuritySettingsFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_container, SecuritySettingsFragment.newInstance())
                    .addToBackStack(SecuritySettingsFragment.class.getSimpleName())
                    .commit();
        }
    }

    private void copyToClipboard() {
        ClipboardManager clipboardManager = getActivity() == null ?
                null : (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            return;
        }
        String applicationInfo = textVersionTitle.getText().toString().concat(Constants.NEW_LINE)
                .concat(textVersionDescription.getText().toString());
        ClipData clipData = ClipData.newPlainText(applicationInfo, applicationInfo);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(getActivity(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_copy)
    public void onClickCopy(View v) {
        v.setEnabled(false);
        v.postDelayed(() -> v.setEnabled(true), 500);
        copyToClipboard();
    }

    @OnClick(R.id.container_security)
    public void onClickSettings() {
        Analytics.getInstance(getActivity()).logSettings(AnalyticsConstants.SETTINGS_SECURITY_SETTINGS);
        if (Prefs.contains(Constants.PREF_LOCK) && Prefs.getBoolean(Constants.PREF_LOCK)) {
            isSettingsClicked = true;
            ((BaseActivity)getActivity()).showLock();
        } else {
            showSecuritySettingsFragment();
        }
    }

    @OnClick(R.id.container_exchange)
    void onClickExchange(View v) {
        v.setEnabled(false);
        v.postDelayed(() -> v.setEnabled(true), 500);
        if (getActivity() != null) {
            ExchangeChooserFragment fragment = (ExchangeChooserFragment) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(ExchangeChooserFragment.TAG);
            if (fragment == null) {
                fragment = ExchangeChooserFragment.getInstance();
            }
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_frame, fragment).addToBackStack(ExchangeChooserFragment.TAG)
                    .commit();
        }
    }

    @OnClick(R.id.container_fiat)
    void onClickFiat(View v) {
        v.setEnabled(false);
        v.postDelayed(() -> v.setEnabled(true), 500);
        if (getActivity() != null) {
            CurrencyChooserFragment fragment = (CurrencyChooserFragment) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(CurrencyChooserFragment.TAG);
            if (fragment == null) {
                fragment = CurrencyChooserFragment.getInstance();
            }
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.full_container, fragment)
                    .addToBackStack(ChainChooserFragment.TAG).commit();
        }
    }

    @OnClick(R.id.container_about)
    public void onClickAbout() {

    }

    @OnClick(R.id.container_feedback)
    public void onClickFeedback() {

    }
}
