/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.fragments.main.AssetsFragment;
import io.multy.ui.fragments.main.ContactsFragment;
import io.multy.ui.fragments.main.FeedFragment;
import io.multy.ui.fragments.main.SettingsFragment;
import io.multy.util.Constants;


public class MainActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    private boolean isFirstFragmentCreation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;

        setupFooter();
        setFragment(R.id.inner_container, AssetsFragment.newInstance());

        startActivity(new Intent(this, SeedActivity.class));
    }

    private void setFragment(@IdRes int container, Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(container, fragment);

        if (!isFirstFragmentCreation) {
            transaction.addToBackStack(fragment.getClass().getName());
        }

        isFirstFragmentCreation = false;
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        unCheckAllTabs();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()) {
            case Constants.POSITION_ASSETS:
                setFragment(R.id.full_container, AssetsFragment.newInstance());
                break;
            case Constants.POSITION_FEED:
                setFragment(R.id.full_container, FeedFragment.newInstance());
                break;
            case Constants.POSITION_CONTACTS:
                setFragment(R.id.full_container, ContactsFragment.newInstance());
                break;
            case Constants.POSITION_SETTINGS:
                setFragment(R.id.full_container, SettingsFragment.newInstance());
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private void setupFooter() {
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.footer_assets));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.footer_feed));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.footer_main));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.footer_contacts));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.footer_settings));
        tabLayout.addOnTabSelectedListener(this);
    }

    ;

    private void unCheckAllTabs() {
        tabLayout.getTabAt(0).getCustomView().setSelected(false);
        tabLayout.getTabAt(1).getCustomView().setSelected(false);
        tabLayout.getTabAt(3).getCustomView().setSelected(false);
        tabLayout.getTabAt(4).getCustomView().setSelected(false);
    }
}
