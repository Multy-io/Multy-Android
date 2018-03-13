/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import io.multy.R;
import io.multy.ui.fragments.asset.CreateAssetFragment;
import io.multy.util.analytics.Analytics;

public class CreateAssetActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_asset);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CreateAssetFragment.TAG);
        if (fragment == null) {
            fragment = CreateAssetFragment.newInstance();
        }
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_main, fragment, CreateAssetFragment.TAG)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Analytics.getInstance(this).logCreateWalletClose();
        super.onBackPressed();
    }
}
