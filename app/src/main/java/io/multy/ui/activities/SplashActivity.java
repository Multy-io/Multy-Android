/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.responses.ServerConfigResponse;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.util.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.container)
    View container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

//        FirstLaunchHelper.preventRootIfDetected(this);

        Animation emergency = AnimationUtils.loadAnimation(this, R.anim.splash_emergency);
        emergency.setDuration(350);
        emergency.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                getServerConfig();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        container.startAnimation(emergency);
    }

    private void getServerConfig() {
        MultyApi.INSTANCE.getServerConfig().enqueue(new Callback<ServerConfigResponse>() {
            @Override
            public void onResponse(Call<ServerConfigResponse> call, Response<ServerConfigResponse> response) {
                if (response.isSuccessful()) {
                    ServerConfigResponse configResponse = response.body();
                    ServerConfigResponse.AndroidConfig androidConfig = configResponse.getAndroidConfig();
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
                        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                        preferences.edit().putString(Constants.PREF_DONATE_ADDRESS_BTC,
                                configResponse.getDonateInfo().getBtcDonateAddress()).apply();
                        preferences.edit().putString(Constants.PREF_DONATE_ADDRESS_ETH,
                                configResponse.getDonateInfo().getEthDonateAddress()).apply();
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

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {

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

        Thread background = new Thread() {
            public void run() {
                try {
                    sleep(500);
                    Intent mainActivityIntent = new Intent(SplashActivity.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    mainActivityIntent.putExtra(MainActivity.IS_ANIMATION_MUST_SHOW, true);
                    startActivity(mainActivityIntent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Intent i=new Intent(getBaseContext(), MainActivity.class);
                    startActivity(i);
                }
            }
        };
        background.start();
    }
}
