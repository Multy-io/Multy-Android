/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.fragments.BaseSeedFragment;
import io.multy.ui.fragments.seed.HelloSeedFragment;
import io.multy.ui.fragments.seed.SeedValidationFragment;
import io.multy.util.Constants;

public class SeedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_seed);
        ButterKnife.bind(this);

        if (getIntent().hasCategory(Constants.EXTRA_RESTORE)) {
            setFragment(new SeedValidationFragment());
        } else {
            setFragment(new HelloSeedFragment());
        }
    }

    private void setFragment(BaseSeedFragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment, fragment.getClass().getSimpleName())
                .commit();
    }
}
