/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.responses.ServerConfigResponse;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        FirstLaunchHelper.preventRootIfDetected(this);

        MultyApi.INSTANCE.getServerConfig().enqueue(new Callback<ServerConfigResponse>() {
            @Override
            public void onResponse(Call<ServerConfigResponse> call, Response<ServerConfigResponse> response) {
                if (response.isSuccessful()) {
                    ServerConfigResponse.AndroidConfig androidConfig = response.body().getAndroidConfig();
                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        int versionCode = pInfo.versionCode;
                        if (versionCode < androidConfig.getSoftVersion()) {
                            //we can still use soft version
                            //leave this clause for future possible purposes
//                            showUpdateDialog();
                            showMainActivity();
                        } else if (versionCode < androidConfig.getHardVersion()) {
                            showUpdateDialog();
                        } else {
                            showMainActivity();
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
//                    showError(R.string.error_config_error);
                    showMainActivity();
                }
            }

            @Override
            public void onFailure(Call<ServerConfigResponse> call, Throwable t) {
//                showError(R.string.error_config_error);
                showMainActivity();
            }
        });
    }

    private void showError(int message) {
        SimpleDialogFragment.newInstanceNegative(R.string.error, message, view -> {
            finish();
        }).show(getSupportFragmentManager(), "");
    }

    private void showUpdateDialog() {
        SimpleDialogFragment.newInstanceNegative(R.string.error, R.string.please_update, view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            finish();
        }).show(getSupportFragmentManager(), "");
    }

    private void showMainActivity() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 500);
    }
}
