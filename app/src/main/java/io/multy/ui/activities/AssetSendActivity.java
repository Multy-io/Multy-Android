/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
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
import android.view.MenuItem;

import java.util.List;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.send.AmountChooserFragment;
import io.multy.ui.fragments.send.AssetSendFragment;
import io.multy.ui.fragments.send.SendSummaryFragment;
import io.multy.ui.fragments.send.TransactionFeeFragment;
import io.multy.ui.fragments.send.WalletChooserFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.AssetSendViewModel;


public class AssetSendActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindInt(R.integer.zero)
    int zero;

    private boolean isFirstFragmentCreation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_send);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setFragment(R.string.send_to, R.id.container, AssetSendFragment.newInstance());
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
                if (backStackFragment instanceof SendSummaryFragment) {
                    toolbar.setTitle(R.string.send);
                } else if (backStackFragment instanceof AmountChooserFragment) {
                    toolbar.setTitle(R.string.transaction_speed);
                } else if (backStackFragment instanceof TransactionFeeFragment) {
                    toolbar.setTitle(R.string.send_from);
                } else if (backStackFragment instanceof WalletChooserFragment) {
                    toolbar.setTitle(R.string.send_to);
                } else if (backStackFragment instanceof AssetSendFragment) {
                    toolbar.setTitle(R.string.send_to);
                }
            }
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
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
    }

    @OnClick(R.id.button_cancel)
    void ocLickCancel(){
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

    public void showScanScreen(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST_CODE);
        } else {
            startActivityForResult(new Intent(this, ScanActivity.class), Constants.CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showScanScreen();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(Constants.EXTRA_QR_CONTENTS)){
                ViewModelProviders.of(this)
                        .get(AssetSendViewModel.class)
                        .setReceiverAddress(data.getStringExtra(Constants.EXTRA_QR_CONTENTS));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

