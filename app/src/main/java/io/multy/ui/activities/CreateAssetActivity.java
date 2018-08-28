/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import io.multy.R;
import io.multy.ui.fragments.ScanInvitationCodeFragment;
import io.multy.ui.fragments.asset.CreateAssetFragment;
import io.multy.ui.fragments.asset.CreateMultisigBlankFragment;
import io.multy.util.analytics.Analytics;

public class CreateAssetActivity extends BaseActivity {

    public static final String EXTRA_MULTISIG = "EXTRA_MULTISIG";
    public static final int EXTRA_CREATE_WALLET = 0;
    public static final int EXTRA_MULTISIG_CREATE = 1;
    public static final int EXTRA_MULTISIG_JOIN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_asset);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        int extraType = getIntent().getIntExtra(EXTRA_MULTISIG, EXTRA_CREATE_WALLET);
        String fragmentTag;
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag(extraType == EXTRA_MULTISIG_CREATE ? CreateMultisigBlankFragment.TAG :
                        extraType == EXTRA_MULTISIG_JOIN ? ScanInvitationCodeFragment.TAG : CreateAssetFragment.TAG);
        if (fragment == null) {
            switch (extraType) {
                case EXTRA_MULTISIG_CREATE:
                    fragment = CreateMultisigBlankFragment.getInstance();
                    fragmentTag = CreateMultisigBlankFragment.TAG;
                    break;
                case EXTRA_MULTISIG_JOIN:
                    fragment = ScanInvitationCodeFragment.getInstance();
                    fragmentTag = ScanInvitationCodeFragment.TAG;
                    break;
                    default:
                        fragment = CreateAssetFragment.getInstance();
                        fragmentTag = CreateAssetFragment.TAG;
            }
        } else {
            fragmentTag = fragment.getTag();
        }
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_main, fragment, fragmentTag)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Analytics.getInstance(this).logCreateWalletClose();
        super.onBackPressed();
    }
}
