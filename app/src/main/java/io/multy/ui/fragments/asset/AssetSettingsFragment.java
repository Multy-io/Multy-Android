/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.WalletViewModel;

/**
 * Created by anschutz1927@gmail.com on 07.12.17.
 */

public class AssetSettingsFragment extends BaseFragment {

    public static final String TAG = AssetSettingsFragment.class.getSimpleName();

    @BindView(R.id.edit_name)
    EditText inputName;

    private WalletViewModel viewModel;

    public static AssetSettingsFragment newInstance() {
        Bundle args = new Bundle();
        AssetSettingsFragment fragment = new AssetSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public AssetSettingsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        setBaseViewModel(viewModel);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_asset_settings, container, false);
//        View v = inflater.inflate(R.layout.view_asset_settings, container, false);
        ButterKnife.bind(this, v);
        viewModel.getWalletLive().observe(this, walletRealmObject -> {
            if (walletRealmObject != null && walletRealmObject.getName() != null) {
                inputName.setText(walletRealmObject.getName());
            }
        });
        inputName.setOnFocusChangeListener((v1, hasFocus) -> {
            if (hasFocus)
            Analytics.getInstance(getActivity()).logWalletSettings(AnalyticsConstants.WALLET_SETTINGS_RENAME, viewModel.getChainId());
        });
        Analytics.getInstance(getActivity()).logWalletSettingsLaunch(viewModel.getChainId());
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        hideKeyboard(getActivity());
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        viewModel.isLoading.setValue(false);
        super.onDestroy();
    }

    private void saveSettings() {
        if (inputName.getText().toString().isEmpty() || viewModel.getWalletLive() == null ||
                viewModel.getWalletLive().getValue() == null ||
                inputName.getText().toString().equals(viewModel.getWalletLive().getValue().getName())) {
            getActivity().onBackPressed();
            return;
        }
        viewModel.isLoading.setValue(true);
        inputName.setEnabled(false);
        viewModel.updateWalletSetting(inputName.getText().toString()).observe(this, isUpdated -> {
            if (isUpdated == null || !isUpdated) {
                Toast.makeText(getActivity(), "Error, changes not applied!", Toast.LENGTH_SHORT).show();
                inputName.setEnabled(true);
                return;
            }
            viewModel.isLoading.setValue(false);
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void chooseCurrencyToConvert() {

    }

    private void showMyPrivateKey() {
        if (getActivity() != null && getActivity() instanceof AssetActivity) {
            ((AssetActivity) getActivity()).setFragment(R.id.container_full, SettingAssetAddressesFragment.getInstance());
        }
    }

    private void deleteWallet() {
        viewModel.removeWallet().observe(this, isRemoved -> {
            if (isRemoved != null && isRemoved) {
                Toast.makeText(getActivity(), R.string.wallet_removed, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
            else if (isRemoved != null) {
                Toast.makeText(getActivity(), "Error, changes not applied!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.button_cancel)
    void onClickCancel() {
        Analytics.getInstance(getActivity()).logWalletSettings(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @OnClick(R.id.button_save)
    void onClickSave(View v) {
        Analytics.getInstance(getActivity()).logWalletSettings(AnalyticsConstants.WALLET_SETTINGS_SAVE, viewModel.getChainId());
        v.setEnabled(false);
        saveSettings();
        v.postDelayed(() -> v.setEnabled(true), 500);
    }

    @OnClick(R.id.button_currency)
    void onClickCurrency(View v) {
        v.setEnabled(false);
        if (getActivity() != null) {
            Analytics.getInstance(getActivity()).logWalletSettings(AnalyticsConstants.WALLET_SETTINGS_FIAT, viewModel.getChainId());
            CurrencyChooserFragment fragment = (CurrencyChooserFragment) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(CurrencyChooserFragment.TAG);
            if (fragment == null) {
                fragment = CurrencyChooserFragment.getInstance();
            }
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container_full, fragment)
                    .addToBackStack(ChainChooserFragment.TAG).commit();
        }
    }

    @OnClick(R.id.button_key)
    void onClickKey(View view) {
        view.setEnabled(false);
        Analytics.getInstance(getActivity()).logWalletSettings(AnalyticsConstants.WALLET_SETTINGS_KEY, viewModel.getChainId());
        showMyPrivateKey();
    }

    @OnClick(R.id.edit_name)
    void onClickName() {
        Analytics.getInstance(getActivity()).logWalletSettings(AnalyticsConstants.WALLET_SETTINGS_RENAME, viewModel.getChainId());
    }

    @OnClick(R.id.button_delete)
    void onClickDelete(View view) {
        Analytics.getInstance(getActivity()).logWalletSettings(AnalyticsConstants.WALLET_SETTINGS_DELETE, viewModel.getChainId());
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);
        SimpleDialogFragment dialogConfirmation = SimpleDialogFragment
                .newInstance(R.string.delete_wallet, R.string.delete_confirm, v -> {
                    Analytics.getInstance(getActivity()).logWalletSettings(AnalyticsConstants.WALLET_SETTINGS_DELETE_YES, viewModel.getChainId());
                    deleteWallet();
                });
        dialogConfirmation.show(getChildFragmentManager(), SimpleDialogFragment.class.getSimpleName());
    }
}
