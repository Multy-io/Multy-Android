/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.WindowManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.send.AmountChooserFragment;
import io.multy.ui.fragments.send.AssetSendFragment;
import io.multy.ui.fragments.send.SendSummaryFragment;
import io.multy.ui.fragments.send.TransactionFeeFragment;
import io.multy.ui.fragments.send.WalletChooserFragment;
import io.multy.ui.fragments.send.ethereum.EthAmountChooserFragment;
import io.multy.ui.fragments.send.ethereum.EthSendSummaryFragment;
import io.multy.ui.fragments.send.ethereum.EthTransactionFeeFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetSendViewModel;
import timber.log.Timber;

import static io.multy.ui.fragments.send.WalletChooserFragment.NO_VALUE;


public class AssetSendActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean isFirstFragmentCreation;
    private AssetSendViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_asset_send);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;
        viewModel = ViewModelProviders.of(this).get(AssetSendViewModel.class);

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
        //TODO WTF is that. fragment backstack, nah?
        logCancel();
        if (getSupportFragmentManager().getBackStackEntryCount() >= 1) {
            List<Fragment> backStackFragments = getSupportFragmentManager().getFragments();
            for (Fragment backStackFragment : backStackFragments) {
                if (backStackFragment instanceof SendSummaryFragment || backStackFragment instanceof EthSendSummaryFragment) {
                    toolbar.setTitle(R.string.send_amount);
                } else if (backStackFragment instanceof AmountChooserFragment || backStackFragment instanceof EthAmountChooserFragment) {
                    toolbar.setTitle(R.string.transaction_fee);
                } else if (backStackFragment instanceof TransactionFeeFragment || backStackFragment instanceof EthTransactionFeeFragment) {
                    toolbar.setTitle(R.string.send_from);
                } else if (backStackFragment instanceof WalletChooserFragment) {
                    toolbar.setTitle(R.string.send_to);
                } else if (backStackFragment instanceof AssetSendFragment || backStackFragment instanceof EthSendSummaryFragment) {
                    toolbar.setTitle(R.string.send_to);
                }
            }
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void startFlow() {
        AssetSendViewModel viewModel = ViewModelProviders.of(this).get(AssetSendViewModel.class);
        if (getIntent().hasExtra(Constants.EXTRA_WALLET_ID)) {
            viewModel.setWallet(RealmManager.getAssetsDao().getWalletById(getIntent().getExtras().getLong(Constants.EXTRA_WALLET_ID, -1)));
        }

        if (getIntent().hasExtra(Constants.EXTRA_ADDRESS)) {
            setFragment(R.string.send_to, R.id.container, AssetSendFragment.newInstance());
            setFragment(R.string.send_from, R.id.container, WalletChooserFragment.newInstance(NO_VALUE, NO_VALUE));
            setTitle(R.string.send_from);
            viewModel.setReceiverAddress(getIntent().getStringExtra(Constants.EXTRA_ADDRESS));
            viewModel.thoseAddress.setValue(getIntent().getStringExtra(Constants.EXTRA_ADDRESS));
            if (getIntent().hasExtra(Constants.EXTRA_AMOUNT)) {
                if (!TextUtils.isEmpty(getIntent().getStringExtra(Constants.EXTRA_AMOUNT))) {
                    Timber.i("amount %s", getIntent().getStringExtra(Constants.EXTRA_AMOUNT));
                    String amountString = getIntent().getStringExtra(Constants.EXTRA_AMOUNT);
                    amountString = amountString.replaceAll(",", ".");
                    double amount = Double.parseDouble(amountString);
                    if (amount > 0) {
                        viewModel.setAmount(amount);
                        viewModel.setAmountScanned(true);
                    }
                }
            }
        } else {
            setFragment(R.string.send_to, R.id.container, AssetSendFragment.newInstance());
        }
    }

    public void setFragment(@StringRes int title, @IdRes int container, Fragment fragment) {
        toolbar.setTitle(title);

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(container, fragment);

        if (!isFirstFragmentCreation) {
            transaction.addToBackStack(fragment.getClass().getName());
        }

        isFirstFragmentCreation = false;
        transaction.commit();

        hideKeyboard(this);
    }

    @OnClick(R.id.button_cancel)
    void ocLickCancel() {
        logCancel();
        finish();
    }

//    private void showAlert(@StringRes int resId, String msg){
//        new AlertDialog.Builder(this)
//                .setTitle(resId)
//                .setMessage(msg)
//                .setCancelable(false)
//                .setPositiveButton(R.string.yes, (dialog, which) -> finish())
//                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
//                .show();
//    }

    public void showScanScreen() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST_CODE);
        } else {
            startActivityForResult(new Intent(this, ScanActivity.class), Constants.CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.CAMERA_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Analytics.getInstance(this).logSendTo(AnalyticsConstants.PERMISSION_GRANTED);
                showScanScreen();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Analytics.getInstance(this).logSendTo(AnalyticsConstants.PERMISSION_DENIED);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(Constants.EXTRA_QR_CONTENTS)) {
                AssetSendViewModel viewModel = ViewModelProviders.of(this).get(AssetSendViewModel.class);
                viewModel.setReceiverAddress(data.getStringExtra(Constants.EXTRA_QR_CONTENTS));
                viewModel.thoseAddress.setValue(data.getStringExtra(Constants.EXTRA_QR_CONTENTS));
                if (data.hasExtra(Constants.EXTRA_AMOUNT) && data.getExtras() != null) {
                    viewModel.setAmountScanned(true);
                    viewModel.setAmount(Double.valueOf(data.getExtras().getString(Constants.EXTRA_AMOUNT, "0")));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void logCancel() {
        List<Fragment> backStackFragments = getSupportFragmentManager().getFragments();
        for (Fragment backStackFragment : backStackFragments) {
            if (backStackFragment instanceof SendSummaryFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logSendSummary(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            } else if (backStackFragment instanceof AmountChooserFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logSendChooseAmount(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            } else if (backStackFragment instanceof TransactionFeeFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logTransactionFee(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            } else if (backStackFragment instanceof WalletChooserFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logSendFrom(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            } else if (backStackFragment instanceof AssetSendFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logSendTo(AnalyticsConstants.BUTTON_CLOSE);
            }
        }
    }
}

