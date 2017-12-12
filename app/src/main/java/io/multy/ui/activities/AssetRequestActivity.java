/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.support.annotation.StringRes;
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
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.receive.AmountChooserFragment;
import io.multy.ui.fragments.receive.WalletChooserFragment;


public class AssetRequestActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindInt(R.integer.zero)
    int zero;

    private boolean isFirstFragmentCreation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_request);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setFragment(R.string.receive, WalletChooserFragment.newInstance());
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
                if (backStackFragment instanceof AddressesFragment) {
                    toolbar.setTitle(R.string.receive);
                }
                if (backStackFragment instanceof AmountChooserFragment) {
                    toolbar.setTitle(R.string.receive);
                }
            }
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.button_cancel)
    void ocLickCancel(){
        finish();
    }

    public void setFragment(@StringRes int title, Fragment fragment) {
        toolbar.setTitle(title);

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment);

        if (!isFirstFragmentCreation) {
            transaction.addToBackStack(fragment.getClass().getName());
        }

        isFirstFragmentCreation = false;
        transaction.commit();
    }
}
