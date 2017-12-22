/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import io.multy.storage.DatabaseHelper;
import io.multy.ui.fragments.BaseSeedFragment;
import io.multy.util.FirstLaunchHelper;
import timber.log.Timber;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onStart() {
        super.onStart();
//        FirstLaunchHelper.preventRootIfDetected(this);

        if (FirstLaunchHelper.isLockModeEnabled()) { // check lock mode
            if (FirstLaunchHelper.isFingerprintEnabled()) { // for first check if fingerprint enabled
                if (FirstLaunchHelper.isFingerprintAttemptsAppropriate()) {
                    new AlertDialog.Builder(this)
//                          showFingerprintPinScreen();
                            .setTitle("Enter PIN")
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    if (FirstLaunchHelper.isPinAttemptsAppropriate()) {
//                        showEnterPinScreen();
                        new AlertDialog.Builder(this)
                                .setTitle("Enter PIN")
                                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                                .show();
                    } else {
//                    showWaitDialog();
                    }
                }
            } else {
                if (FirstLaunchHelper.isPinAttemptsAppropriate()) {
//                    showEnterPinScreen();
                } else {
//                    showWaitDialog();
                }
            }
        } else {

        }

        // if we can create instance of Database, it can be decrypted.
        // Otherwise database is deleted with exiting app.
        new DatabaseHelper(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        }.start();

    }

}
