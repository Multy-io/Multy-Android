/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.fragments.seed.HelloSeedFragment;
import io.multy.ui.fragments.seed.SeedFragment;

public class SeedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_seed);
        ButterKnife.bind(this);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, new HelloSeedFragment(), HelloSeedFragment.class.getSimpleName())
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SeedFragment.class.getSimpleName());
        if (fragment != null) {
            ((SeedFragment) fragment).onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
