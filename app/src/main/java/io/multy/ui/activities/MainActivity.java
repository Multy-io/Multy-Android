/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;


import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.scottyab.rootbeer.RootBeer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.ui.fragments.main.AssetsFragment;
import io.multy.ui.fragments.main.ContactsFragment;
import io.multy.ui.fragments.main.FeedFragment;
import io.multy.ui.fragments.main.SettingsFragment;
import io.multy.util.Constants;


public class MainActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    private boolean isFirstFragmentCreation;
    private int lastTabPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;
        setupFooter();
        setFragment(R.id.container_frame, AssetsFragment.newInstance());

        preventRootIfDetected();
    }

    private void preventRootIfDetected() {
        RootBeer rootBeer = new RootBeer(this);
        if (rootBeer.isRootedWithoutBusyBoxCheck()) {
            SimpleDialogFragment.newInstanceNegative(R.string.root_title, R.string.root_message, view -> finish())
                    .show(getSupportFragmentManager(), "");
        }
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
        changeStateLastTab(lastTabPosition, false);
        lastTabPosition = tab.getPosition();
        changeStateLastTab(lastTabPosition, true);
        switch (tab.getPosition()) {
            case Constants.POSITION_ASSETS:
                setFragment(R.id.container_frame, AssetsFragment.newInstance());
                break;
            case Constants.POSITION_FEED:
                setFragment(R.id.container_frame, FeedFragment.newInstance());
                break;
            case Constants.POSITION_CONTACTS:
                setFragment(R.id.container_frame, ContactsFragment.newInstance());
                break;
            case Constants.POSITION_SETTINGS:
                setFragment(R.id.container_frame, SettingsFragment.newInstance());
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @SuppressLint("ClickableViewAccessibility")
    private void disableEmptyLabItem() {
        ViewGroup container = (ViewGroup) tabLayout.getChildAt(0);
        if (container == null) {
            return;
        }
        View emptyView = container.getChildCount() >= 2 ? container.getChildAt(2) : null;
        if (emptyView != null) {
            emptyView.setOnTouchListener((view, motionEvent) -> true);
        }
    }

    private void setupFooter() {
        tabLayout.addOnTabSelectedListener(this);
        disableEmptyLabItem();
    }

    /**
     * This method change color of selected or unselected item.
     * Set parameter @mustEnable true to set icon and text to "enable" color.
     * Set parameter @mustEnable false to set icon and text to "disable" color.
     *
     * @param position   element position that must change color
     * @param mustEnable true to set icon and text to "enable" color
     */
    private void changeStateLastTab(int position, boolean mustEnable) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab == null) {
            return;
        }
        View view = tab.getCustomView();
        if (view == null) {
            return;
        }
        TextView title = view.findViewById(R.id.title);
        ImageView image = view.findViewById(R.id.image_logo);
        int filterColor;
        if (mustEnable) {
            filterColor = ContextCompat.getColor(this, R.color.tab_active);
        } else {
            filterColor = ContextCompat.getColor(this, R.color.tab_inactive);
        }
        title.setTextColor(filterColor);
        image.setColorFilter(filterColor, PorterDuff.Mode.SRC_IN);
    }

    private void unCheckAllTabs() {
        tabLayout.getTabAt(0).getCustomView().setSelected(false);
        tabLayout.getTabAt(1).getCustomView().setSelected(false);
        tabLayout.getTabAt(3).getCustomView().setSelected(false);
        tabLayout.getTabAt(4).getCustomView().setSelected(false);
    }

    @OnClick(R.id.fast_operations)
    void onFastOperationsClick() {

    }
}
