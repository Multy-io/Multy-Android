/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import io.multy.util.FirstLaunchHelper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
//        FirstLaunchHelper.preventRootIfDetected(this);

        if (FirstLaunchHelper.isLockModeEnabled()) { // check lock mode
            if (FirstLaunchHelper.isFingerprintEnabled()) { // for first check if fingerprint enabled
                if (FirstLaunchHelper.isPinAttemptsAppropriate()) {
//                        showEnterPinScreen();
                    new AlertDialog.Builder(this)
                            .setTitle("Enter PIN")
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
//                    showWaitDialog();
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

    }

    @Override
    protected void onResume() {
        super.onResume();

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 1000);
    }

}
