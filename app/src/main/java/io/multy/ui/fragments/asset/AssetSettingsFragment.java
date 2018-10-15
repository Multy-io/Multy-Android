/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.douglasjunior.androidSimpleTooltip.ArrowDrawable;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;
import io.multy.R;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.DeleteAssetDialogFragment;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.WalletViewModel;

public class AssetSettingsFragment extends BaseFragment {

    public static final String TAG = AssetSettingsFragment.class.getSimpleName();

    @BindView(R.id.edit_name)
    EditText inputName;
    @BindView(R.id.container_params)
    ViewStub stubParams;
    @BindView(R.id.group_imported_wallet)
    Group groupImportedWallet;


    @Nullable
    private WalletParams walletParams;

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
        viewModel = ViewModelProviders.of(requireActivity()).get(WalletViewModel.class);
        setBaseViewModel(viewModel);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_asset_settings, container, false);
        ButterKnife.bind(this, v);
        viewModel.getWalletLive().observe(this, walletRealmObject -> {
            if (walletRealmObject != null && walletRealmObject.getWalletName() != null) {
                inputName.setText(walletRealmObject.getWalletName());
                groupImportedWallet.setVisibility(walletRealmObject.shouldUseExternalKey() ? View.VISIBLE : View.GONE);
                if (walletRealmObject.getCurrencyId() == NativeDataHelper.Blockchain.EOS.getValue() && stubParams.getParent() != null) {  //for multisig need to set
                    stubParams.setLayoutResource(R.layout.view_wallet_parameters);                      //multisig wallet params
                    walletParams = new WalletParams(this.stubParams.inflate());                         //view layout id and create
                    walletParams.textRam.setText("150"); //todo change notifications
                    walletParams.textCpu.setText("36");
                    walletParams.textNet.setText("224");
                }
            }
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
        if (inputName.getText().toString().isEmpty() || viewModel.getWalletLive().getValue() == null ||
                inputName.getText().toString().equals(viewModel.getWalletLive().getValue().getWalletName())) {
            getActivity().onBackPressed();
            return;
        }
        viewModel.isLoading.setValue(true);
        inputName.setEnabled(false);
        viewModel.updateWalletSetting(inputName.getText().toString()).observe(this, isUpdated -> {
            Analytics.getInstance(getActivity()).logWalletSettings(AnalyticsConstants.WALLET_SETTINGS_RENAME, viewModel.getChainId());
            if (isUpdated == null || !isUpdated) {
                Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                inputName.setEnabled(true);
                return;
            }
            viewModel.isLoading.setValue(false);
            ((AssetActivity) getActivity()).setWalletName(inputName.getText().toString());
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
            } else if (isRemoved != null) {
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
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

    @OnClick(R.id.button_resync)
    void onClickResync(View view) {
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);
        viewModel.resyncWallet(() -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    @OnClick(R.id.button_delete)
    void onClickDelete(View view) {
        Analytics.getInstance(getActivity()).logWalletSettings(AnalyticsConstants.WALLET_SETTINGS_DELETE, viewModel.getChainId());
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);
        DeleteAssetDialogFragment.getInstance(() -> {
            Analytics.getInstance(getActivity()).logWalletSettings(AnalyticsConstants.WALLET_SETTINGS_DELETE_YES, viewModel.getChainId());
            deleteWallet();
        }).show(getChildFragmentManager(), DeleteAssetDialogFragment.TAG);
    }

    class WalletParams {

        @BindView(R.id.text_ram)
        TextView textRam;
        @BindView(R.id.text_cpu)
        TextView textCpu;
        @BindView(R.id.text_net)
        TextView textNet;

        private SimpleTooltip.Builder simpleTooltipBuilder;

        WalletParams(View view) {
            ButterKnife.bind(this, view);
        }

        private void showPopup(View view, @StringRes int messageId) {
            float maxViewWidth = view.getX() - TypedValue
                    .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
            getTooltipInstance().anchorView(view)
                    .text(messageId)
                    .maxWidth(maxViewWidth)
                    .build().show();
        }

        private SimpleTooltip.Builder getTooltipInstance() {
            if (simpleTooltipBuilder == null) {
                Context context = textCpu.getContext();
                simpleTooltipBuilder = new SimpleTooltip.Builder(getContext())
                        .padding(0f)
                        .arrowColor(ContextCompat.getColor(context, R.color.blue_transparent))
                        .arrowDirection(ArrowDrawable.AUTO)
                        .arrowHeight(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))
                        .arrowWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()))
                        .contentView(getLayoutInflater().inflate(R.layout.popup_notification, null), R.id.text_notification)
                        .gravity(Gravity.START)
                        .animated(false)
                        .transparentOverlay(true)
                        .dismissOnInsideTouch(false)
                        .dismissOnOutsideTouch(true)
                        .focusable(true)
                        .textColor(ContextCompat.getColor(context, R.color.white));
            }
            return simpleTooltipBuilder;
        }

        @OnClick(R.id.notification_ram)
        void onClickHelpRam(View view) {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true), 500);
            showPopup(view, R.string.ram_notification);
        }

        @OnClick(R.id.notification_cpu)
        void onClickHelpCpu(View view) {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true), 500);
            showPopup(view, R.string.cpu_notification);
        }

        @OnClick(R.id.notification_net)
        void onClickHelpNet(View view) {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true), 500);
            showPopup(view, R.string.net_notification);
        }
    }
}
