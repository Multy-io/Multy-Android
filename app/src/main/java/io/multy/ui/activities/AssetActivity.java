/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.asset.AssetInfoFragment;
import io.multy.ui.fragments.asset.EthTransactionInfoFragment;
import io.multy.ui.fragments.asset.TransactionInfoFragment;
import io.multy.ui.fragments.dialogs.DonateDialog;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.WalletViewModel;

import static io.multy.ui.fragments.asset.TransactionInfoFragment.MODE_RECEIVE;

public class AssetActivity extends BaseActivity {

    private boolean isFirstFragmentCreation;
    private WalletViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_info);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;

        viewModel = ViewModelProviders.of(this).get(WalletViewModel.class);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Wallet wallet = viewModel.getWallet(getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0));

        if (getIntent().hasExtra(Constants.EXTRA_TX_HASH)) {
            if (wallet.getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()) {
                setFragment(R.id.container_full, TransactionInfoFragment.newInstance(createBundle(wallet)));
            } else {
                setFragment(R.id.container_full, EthTransactionInfoFragment.newInstance(createBundle(wallet)));
            }
        } else {
            setFragment(R.id.frame_container, new AssetInfoFragment());
        }
    }

    private Bundle createBundle(Wallet wallet) {
        Bundle transactionInfo = new Bundle();
        transactionInfo.putString(Constants.EXTRA_TX_HASH, getIntent().getStringExtra(Constants.EXTRA_TX_HASH));
        transactionInfo.putInt(TransactionInfoFragment.SELECTED_POSITION, TransactionInfoFragment.NO_POSITION);
        transactionInfo.putInt(TransactionInfoFragment.TRANSACTION_INFO_MODE, MODE_RECEIVE);
        transactionInfo.putLong(TransactionInfoFragment.WALLET_INDEX, wallet.getId());
        return transactionInfo;
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

    public void setWalletName(String name) {
        List<Fragment> backStackFragments = getSupportFragmentManager().getFragments();
        for (Fragment backStackFragment : backStackFragments) {
            if (backStackFragment instanceof AssetInfoFragment) {
                ((AssetInfoFragment) backStackFragment).setWalletName(name);
                break;
            }
        }
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
