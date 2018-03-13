/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.receive.AmountChooserFragment;
import io.multy.ui.fragments.receive.RequestSummaryFragment;
import io.multy.ui.fragments.receive.WalletChooserFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetRequestViewModel;


public class AssetRequestActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindInt(R.integer.zero)
    int zero;
    @BindInt(R.integer.one_negative)
    int oneNegative;

    private boolean isFirstFragmentCreation;
    private AssetRequestViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_request);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;
        viewModel = ViewModelProviders.of(this).get(AssetRequestViewModel.class);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        startFlow();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > zero) {
            List<Fragment> backStackFragments = getSupportFragmentManager().getFragments();
            for (Fragment backStackFragment : backStackFragments) {
                if (backStackFragment instanceof AddressesFragment) {
                    toolbar.setTitle(R.string.receive_summary);
                }
                if (backStackFragment instanceof AmountChooserFragment) {
                    toolbar.setTitle(R.string.receive_summary);
                }
                if (backStackFragment instanceof RequestSummaryFragment) {
                    toolbar.setTitle(R.string.receive);
                }
                if (backStackFragment instanceof WalletChooserFragment) {
                    toolbar.setTitle(R.string.receive_summary);
                }
            }
        }
        logCancel();
        super.onBackPressed();
    }

    @OnClick(R.id.button_cancel)
    void ocLickCancel() {
        logCancel();
        finish();
    }

    private void startFlow() {
        if (getIntent().hasExtra(Constants.EXTRA_WALLET_ID)) {
            if (getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, -1) != -1) {
                AssetRequestViewModel viewModel = ViewModelProviders.of(this).get(AssetRequestViewModel.class);
//                viewModel.setContext(this);
                viewModel.getWallet(getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, -1));
                viewModel.getWalletLive().observe(this, walletRealmObject -> setFragment(R.string.receive_summary, RequestSummaryFragment.newInstance()));
            } else {
                Toast.makeText(this, "Invalid wallet index", Toast.LENGTH_SHORT).show();
            }
        } else {
            setFragment(R.string.receive, WalletChooserFragment.newInstance());
        }
    }

    public void setFragment(@StringRes int title, Fragment fragment) {
        toolbar.setTitle(title);

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment);

        if (!isFirstFragmentCreation) {
            transaction.addToBackStack(fragment.getClass().getName());
        }

        isFirstFragmentCreation = false;
        transaction.commit();
    }

    private void logCancel() {
        List<Fragment> backStackFragments = getSupportFragmentManager().getFragments();
        for (Fragment backStackFragment : backStackFragments) {
            if (backStackFragment instanceof AddressesFragment) {
//                    Analytics.getInstance(this).logWalletAddresses(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            }
            if (backStackFragment instanceof RequestSummaryFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logReceiveSummary(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            }
            if (backStackFragment instanceof WalletChooserFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logReceive(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            }
        }
    }
}
