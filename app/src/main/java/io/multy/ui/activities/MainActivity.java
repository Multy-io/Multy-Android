/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.branch.referral.Branch;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.Fee;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.main.AssetsFragment;
import io.multy.ui.fragments.main.ContactsFragment;
import io.multy.ui.fragments.main.FastOperationsFragment;
import io.multy.ui.fragments.main.FeedFragment;
import io.multy.ui.fragments.main.SettingsFragment;
import io.multy.util.AnimationUtils;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import timber.log.Timber;


public class MainActivity extends BaseActivity implements TabLayout.OnTabSelectedListener, BaseActivity.OnLockCloseListener {

    public static final String IS_ANIMATION_MUST_SHOW = "isanimationmustshow";

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.fast_operations)
    View buttonOperations;

    @BindView(R.id.splash)
    View splash;

    private int lastTabPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupFooter();
        onTabSelected(tabLayout.getTabAt(0));

        subscribeToPushNotifications();

        if (Prefs.getBoolean(Constants.PREF_LOCK)) {
            showLock();
        }
        logFirstLaunch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOnLockCLoseListener(this);
        overridePendingTransition(0, 0);

        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
            tabLayout.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.tab_layout_height);
            if (!isLockVisible) {
                buttonOperations.setVisibility(View.VISIBLE);
            }
        } else {
            tabLayout.getLayoutParams().height = 0;
            buttonOperations.setVisibility(View.GONE);
        }

        // Sometimes i had have got repeating of animation after press back button without check by intent
        if (getIntent().getBooleanExtra(IS_ANIMATION_MUST_SHOW, false)) {
            getIntent().putExtra(IS_ANIMATION_MUST_SHOW, false);
            splash.setVisibility(View.VISIBLE);
            new Handler(getMainLooper()).postDelayed(() -> splash.animate().alpha(0).scaleY(4)
                    .scaleX(4).setDuration(400).withEndAction(() -> splash.setVisibility(View.GONE))
                    .start(), 300);
        }

        if (Prefs.getBoolean(Constants.PREF_LOCK)) {
            Fragment fastOperations = getSupportFragmentManager().findFragmentByTag(FastOperationsFragment.TAG);
            if (fastOperations != null) {
                getSupportFragmentManager().beginTransaction().remove(fastOperations).commit();
            }
        }
        checkDeepLink(getIntent());
    }

    @Override
    public void onStop() {
        setOnLockCLoseListener(null);
        super.onStop();
    }

    private void setFragment(@IdRes int container, Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(container, fragment)
                .commit();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        changeStateLastTab(lastTabPosition, false);
        lastTabPosition = tab.getPosition();
        changeStateLastTab(lastTabPosition, true);
        switch (tab.getPosition()) {
            case Constants.POSITION_ASSETS:
                if (!Prefs.getBoolean(Constants.PREF_APP_INITIALIZED, true)) {
                    Analytics.getInstance(this).logMainTab();
                }
                setFragment(R.id.container_frame, AssetsFragment.newInstance());
                break;
            case Constants.POSITION_FEED:
                Analytics.getInstance(this).logActivityTab();
                setFragment(R.id.container_frame, FeedFragment.newInstance());
                break;
            case Constants.POSITION_CONTACTS:
                Analytics.getInstance(this).logContactsTab();
                setFragment(R.id.container_frame, ContactsFragment.newInstance());
                break;
            case Constants.POSITION_SETTINGS:
                Analytics.getInstance(this).logSettingsTab();
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
        try {
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
                filterColor = ContextCompat.getColor(this, R.color.blue);
            } else {
                filterColor = ContextCompat.getColor(this, R.color.blue_light);
            }
            title.setTextColor(filterColor);
            image.setColorFilter(filterColor, PorterDuff.Mode.SRC_IN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unCheckAllTabs() {
        tabLayout.getTabAt(0).getCustomView().setSelected(false);
        tabLayout.getTabAt(1).getCustomView().setSelected(false);
        tabLayout.getTabAt(3).getCustomView().setSelected(false);
        tabLayout.getTabAt(4).getCustomView().setSelected(false);
    }

    @OnClick(R.id.fast_operations)
    void onFastOperationsClick(final View v) {
        Analytics.getInstance(this).logFastOperationsTab();
        v.setEnabled(false);
        v.postDelayed(() -> v.setEnabled(true), AnimationUtils.DURATION_MEDIUM * 2);
        Fragment fastOperationsFragment = getSupportFragmentManager().findFragmentByTag(FastOperationsFragment.TAG);

        if (fastOperationsFragment == null) {
            fastOperationsFragment = FastOperationsFragment.newInstance(
                    (int) buttonOperations.getX() + buttonOperations.getWidth() / 2,
                    (int) buttonOperations.getY() + buttonOperations.getHeight() / 2);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.full_container, fastOperationsFragment, FastOperationsFragment.TAG)
                .addToBackStack(FastOperationsFragment.TAG)
                .commit();
    }

    public void showScanScreen() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST_CODE);
        } else {
            startActivityForResult(new Intent(this, ScanActivity.class), Constants.CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Analytics.getInstance(this).logFastOperations(AnalyticsConstants.FAST_OPERATIONS_PERMISSION_GRANTED);
                showScanScreen();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Analytics.getInstance(this).logFastOperations(AnalyticsConstants.FAST_OPERATIONS_PERMISSION_DENIED);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(Constants.EXTRA_QR_CONTENTS)) {
                startActivity(new Intent(this, AssetSendActivity.class)
                        .putExtra(Constants.EXTRA_ADDRESS, data.getStringExtra(Constants.EXTRA_QR_CONTENTS))
                        .putExtra(Constants.EXTRA_AMOUNT, data.getStringExtra(Constants.EXTRA_AMOUNT)));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        FastOperationsFragment fragment = (FastOperationsFragment) getSupportFragmentManager().findFragmentByTag(FastOperationsFragment.TAG);
        if (fragment != null && !fragment.isCanceling()) {
            fragment.cancel();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null) {
            getIntent().putExtras(intent.getExtras());
        }
    }

    private void checkDeepLink(Intent intent) {
        if (!isLockVisible
                && Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)
                && RealmManager.getAssetsDao().getWallets().size() > 0) {
            if (intent.hasExtra(Constants.EXTRA_ADDRESS)) {
                String addressUri = intent.getStringExtra(Constants.EXTRA_ADDRESS);
                Intent sendLauncher = new Intent(this, AssetSendActivity.class);
                sendLauncher.putExtra(Constants.EXTRA_ADDRESS, addressUri.substring(addressUri.indexOf(":") + 1, addressUri.length()));
                if (intent.hasExtra(Constants.EXTRA_ADDRESS)) {
                    sendLauncher.putExtra(Constants.EXTRA_AMOUNT, intent.getStringExtra(Constants.EXTRA_AMOUNT));
                }
                intent.removeExtra(Constants.EXTRA_ADDRESS);
                intent.removeExtra(Constants.EXTRA_AMOUNT);
                startActivity(sendLauncher);
            }
        }
    }

    @Override
    public void onLockClosed() {
        checkDeepLink(getIntent());
    }

    private void logFirstLaunch() {
        if (!Prefs.getBoolean(Constants.PREF_APP_INITIALIZED, false)) {
            Analytics.getInstance(this).logFirstLaunch();
        }
    }
}
