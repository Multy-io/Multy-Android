/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.asset.AssetInfoFragment;
import io.multy.ui.fragments.asset.TransactionInfoFragment;
import io.multy.ui.fragments.dialogs.DonateThisDialog;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.WalletViewModel;

public class AssetActivity extends BaseActivity {

    private boolean isFirstFragmentCreation;

    private WalletRealmObject wallet;
    private WalletViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_info);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;

        viewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        wallet = RealmManager.getAssetsDao().getWalletById(getIntent().getExtras().getInt(Constants.EXTRA_WALLET_ID, 0));

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setFragment(R.id.frame_container, AssetInfoFragment.newInstance());
    }

    @Override
    protected void onDestroy() {
        ViewModelProviders.of(this).get(WalletViewModel.class).destroy();
        super.onDestroy();
    }

    @OnClick(R.id.send)
    void onClickSend() {
        Analytics.getInstance(this).logWallet(AnalyticsConstants.WALLET_SEND, viewModel.getChainId());
        startActivity(new Intent(this, AssetSendActivity.class)
                .addCategory(Constants.EXTRA_SENDER_ADDRESS)
                .putExtra(Constants.EXTRA_WALLET_ID, getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, 0)));
    }

    @OnClick(R.id.receive)
    void onClickReceive() {
        Analytics.getInstance(this).logWallet(AnalyticsConstants.WALLET_RECEIVE, viewModel.getChainId());
        startActivity(new Intent(this, AssetRequestActivity.class)
                .putExtra(Constants.EXTRA_WALLET_ID, getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, 0)));
    }

    @OnClick(R.id.exchange)
    void onClickExchange() {
        Analytics.getInstance(this).logWallet(AnalyticsConstants.WALLET_EXCHANGE, viewModel.getChainId());
//        Toast.makeText(this, R.string.not_implemented, Toast.LENGTH_SHORT).show();
        DonateThisDialog.getInstance().show(getSupportFragmentManager(), DonateThisDialog.TAG);
    }

    public void setFragment(@IdRes int container, Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(container, fragment);

        if (!isFirstFragmentCreation) {
            transaction.addToBackStack(fragment.getClass().getName());
        }

        isFirstFragmentCreation = false;
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        logCancel();
        super.onBackPressed();
    }

    private void logCancel() {
        List<Fragment> backStackFragments = getSupportFragmentManager().getFragments();
        for (Fragment backStackFragment : backStackFragments) {
            if (backStackFragment instanceof AddressesFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logWalletAddresses(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
                break;
//            } else if (backStackFragment instanceof AssetSettingsFragment && backStackFragment.isVisible()) {
//                Analytics.getInstance(this).logWalletSettings(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
//                break;
            } else if (backStackFragment instanceof TransactionInfoFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logWalletTransaction(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
                break;
            } else if (backStackFragment instanceof AssetInfoFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logWallet(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
                break;
            }
        }
    }


}
