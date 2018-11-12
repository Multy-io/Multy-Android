/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.samwolfand.oneprefs.Prefs;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.OpenDragonsEvent;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.Web3Fragment;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.ui.fragments.main.AssetsFragment;
import io.multy.ui.fragments.main.FastOperationsFragment;
import io.multy.ui.fragments.main.FeedFragment;
import io.multy.ui.fragments.main.SettingsFragment;
import io.multy.ui.fragments.main.contacts.ContactInfoFragment;
import io.multy.ui.fragments.main.contacts.ContactsFragment;
import io.multy.util.Constants;
import io.multy.util.ContactUtils;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.WalletViewModel;


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

    @Subscribe()
    public void onMessageEvent(OpenDragonsEvent event) {
        selectBrowserTab();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkContactAction();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOnLockCLoseListener(this);
        overridePendingTransition(0, 0);

        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED) || getIntent().hasExtra(Constants.EXTRA_URL)) {
            tabLayout.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.tab_layout_height);
            if (!super.isLockVisible()) {
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
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        changeStateLastTab(lastTabPosition, false);
        lastTabPosition = tab.getPosition();
        changeStateLastTab(lastTabPosition, true);
        switch (tab.getPosition()) {
            case Constants.POSITION_ASSETS:
                if (!Prefs.getBoolean(Constants.PREF_APP_INITIALIZED, true)) {
                    Analytics.getInstance(this).logMain(AnalyticsConstants.TAB_MAIN);
                }
                setFragment(R.id.container_frame, AssetsFragment.newInstance());
                break;
            case Constants.POSITION_FEED:
                Analytics.getInstance(this).logMain(AnalyticsConstants.TAB_ACTIVITY);
                setFragment(R.id.container_frame, Web3Fragment.newInstance());
                break;
            case Constants.POSITION_CONTACTS:
                Analytics.getInstance(this).logMain(AnalyticsConstants.TAB_CONTACTS);
                setFragment(R.id.container_frame, ContactsFragment.newInstance());
                break;
            case Constants.POSITION_SETTINGS:
                Analytics.getInstance(this).logMain(AnalyticsConstants.TAB_SETTINGS);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Analytics.getInstance(this).logFastOperations(AnalyticsConstants.PERMISSION_GRANTED);
                showScanScreen();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Analytics.getInstance(this).logFastOperations(AnalyticsConstants.PERMISSION_DENIED);
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
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_frame);

        //TODO
        if (fragment instanceof Web3Fragment) {
            ((Web3Fragment) fragment).onBackPressed();
            return;
        }

        FastOperationsFragment operationsFragment = (FastOperationsFragment) getSupportFragmentManager().findFragmentByTag(FastOperationsFragment.TAG);
        if (operationsFragment != null && !operationsFragment.isCanceling()) {
            operationsFragment.cancel();
        } else {
            super.onBackPressed();
        }
        logCancel();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null) {
            getIntent().putExtras(intent.getExtras());
        }
    }

    @Override
    public void onLockClosed() {
        checkDeepLink(getIntent());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void disableEmptyTabItem() {
        ViewGroup container = (ViewGroup) tabLayout.getChildAt(0);
        if (container == null) {
            return;
        }
        View emptyView = container.getChildCount() >= 2 ? container.getChildAt(2) : null;
        if (emptyView != null) {
            emptyView.setOnTouchListener((view, motionEvent) -> true);
        }
    }

    private void checkContactAction() {
        if (getIntent() != null && getIntent().hasExtra(ContactUtils.EXTRA_ACTION)) {
            int action = getIntent().getIntExtra(ContactUtils.EXTRA_ACTION, ContactUtils.EXTRA_ACTION_OPEN_CONTACTS);
            switch (action) {
                case ContactUtils.EXTRA_ACTION_OPEN_CONTACT: {
                    TabLayout.Tab tab = tabLayout.getTabAt(Constants.POSITION_CONTACTS);
                    if (tab != null) {
                        tab.select();
                    }
                    long contactId = getIntent().getLongExtra(ContactUtils.EXTRA_RAW_CONTACT_ID, 0);
                    if (contactId != 0) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.full_container, ContactInfoFragment.getInstance(contactId))
                                .addToBackStack(getClass().getSimpleName())
                                .commit();
                    }
                    break;
                }
                case ContactUtils.EXTRA_ACTION_OPEN_SEND: {
                    TabLayout.Tab tab = tabLayout.getTabAt(Constants.POSITION_ASSETS);
                    if (tab != null) {
                        tab.select();
                    }
                    final String address = getIntent().getStringExtra(Constants.EXTRA_ADDRESS);
                    startActivity(new Intent(this, AssetSendActivity.class)
                            .putExtra(Constants.EXTRA_ADDRESS, address)
                            .putExtra(Constants.EXTRA_AMOUNT, ""));
                    break;
                }
                default:
                    TabLayout.Tab tab = tabLayout.getTabAt(Constants.POSITION_CONTACTS);
                    if (tab != null) {
                        tab.select();
                    }
            }
            getIntent().removeExtra(ContactUtils.EXTRA_ACTION);
        }
    }

    private void setFragment(@IdRes int container, Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(container, fragment)
                .commit();
    }

    private void setupFooter() {
        tabLayout.addOnTabSelectedListener(this);
        disableEmptyTabItem();
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

    private void checkDeepLink(Intent intent) {
        if (!super.isLockVisible() && (checkSenderDeepLink(intent) || checkBrowserDeepLink(intent)) || checkDeepMagic()) {
            Log.i(getClass().getSimpleName(), "Deep Link detected!");
        }
    }

    private boolean checkDeepMagic() {
        if (getIntent().hasExtra(Constants.EXTRA_DEEP_MAGIC)) {
            final String amount = getIntent().getStringExtra(Constants.EXTRA_AMOUNT);
            final String walletName = getIntent().getStringExtra(Constants.EXTRA_WALLET_NAME);
            final int blockChain = getIntent().getIntExtra(Constants.EXTRA_BLOCK_CHAIN, 0);
            final int networkId = getIntent().getIntExtra(Constants.EXTRA_NETWORK_ID, 0);

            showProgressDialog();

            Wallet wallet = new Wallet();
            wallet.setWalletName(walletName);
            wallet.setCurrencyId(blockChain);
            wallet.setNetworkId(networkId);

            AssetsFragment fragment = (AssetsFragment) getSupportFragmentManager().findFragmentById(R.id.container_frame);

            if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
                Wallet foundWallet = RealmManager.getAssetsDao().getWalletByNameExt(blockChain, networkId, walletName);
                if (foundWallet == null) {
                    fragment.setDeepMagicWallet(wallet);
                } else {
                    getIntent().putExtra(Constants.EXTRA_ADDRESS, foundWallet.getActiveAddress().getAddress());
                    getIntent().putExtra(Constants.EXTRA_WALLET_ID, foundWallet.getId());
                    fragment.startMagicReceive();
                }
            } else {
                WalletViewModel viewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
                viewModel.createFirstWallets(() -> {
                    if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED, false)) {
                        subscribeToPushNotifications();
                    } else {
                        SimpleDialogFragment.newInstanceNegative(R.string.error, R.string.something_went_wrong, v -> {
                            //todo should we clear database here for don't get realm exception when we will want open it?
//                    startActivity(new Intent(this, SplashActivity.class).putExtra(SplashActivity.RESET_FLAG, true));
                        }).show(getSupportFragmentManager(), "");
                    }
                    if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
                        tabLayout.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.tab_layout_height);
                        buttonOperations.setVisibility(View.VISIBLE);
                    }

                    AssetsFragment assetsFragment = AssetsFragment.newInstance();
                    assetsFragment.setDeepMagicWallet(wallet);
                    setFragment(R.id.container_frame, assetsFragment);
                });
            }

            return true;
        }

        return false;
    }

    private boolean checkSenderDeepLink(Intent intent) {
        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED) &&
                RealmManager.getAssetsDao().getWallets().size() > 0 && intent.hasExtra(Constants.EXTRA_ADDRESS)) {

            if (getIntent().hasExtra(Constants.EXTRA_DEEP_BROWSER) ||
                    getIntent().hasExtra(Constants.EXTRA_DEEP_MAGIC)) {
                return false;
            }

            String addressUri = intent.getStringExtra(Constants.EXTRA_ADDRESS);
            Intent sendLauncher = new Intent(this, AssetSendActivity.class);
            sendLauncher.putExtra(Constants.EXTRA_ADDRESS, addressUri.substring(addressUri.indexOf(":") + 1, addressUri.length()));
            if (intent.hasExtra(Constants.EXTRA_AMOUNT)) {
                sendLauncher.putExtra(Constants.EXTRA_AMOUNT, intent.getStringExtra(Constants.EXTRA_AMOUNT));
                intent.removeExtra(Constants.EXTRA_AMOUNT);
            }
            intent.removeExtra(Constants.EXTRA_ADDRESS);
            startActivity(sendLauncher);
            return true;
        }
        return false;
    }

    private boolean checkBrowserDeepLink(Intent intent) {
        if (intent.hasExtra(Constants.EXTRA_URL) && intent.hasExtra(Constants.EXTRA_DEEP_BROWSER)) {
            selectBrowserTab();
            intent.removeExtra(Constants.EXTRA_URL);
            return true;
        }
        return false;
    }

    private void selectBrowserTab() {
        TabLayout.Tab mainTab = tabLayout.getTabAt(0);
        TabLayout.Tab browserTab = tabLayout.getTabAt(1);
        if (browserTab != null) {
            if (browserTab.isSelected() && mainTab != null) {
                mainTab.select();
            }
            browserTab.select();
        }
    }

    private void logFirstLaunch() {
        if (!Prefs.getBoolean(Constants.PREF_APP_INITIALIZED, false)) {
            Analytics.getInstance(this).logFirstLaunch();
        }
    }

    private void logCancel() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_frame);
        if (fragment instanceof FeedFragment && fragment.isVisible()) {
            Analytics.getInstance(this).logActivityClose();
        } else if (fragment instanceof ContactsFragment && fragment.isVisible()) {
            Analytics.getInstance(this).logContactsClose();
        } else if (fragment instanceof AssetsFragment && fragment.isVisible()) {
            Analytics.getInstance(this).logMain(AnalyticsConstants.BUTTON_CLOSE);
        } else if (fragment instanceof SettingsFragment && fragment.isVisible()) {
            Analytics.getInstance(this).logSettings(AnalyticsConstants.BUTTON_CLOSE);
        }
    }

    private void updateAssets() {
        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
            tabLayout.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.tab_layout_height);
            buttonOperations.setVisibility(View.VISIBLE);
        }
        setFragment(R.id.container_frame, AssetsFragment.newInstance());
    }

    public void createFirstWallets() {
        WalletViewModel viewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        viewModel.createFirstWallets(() -> {
            if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED, false)) {
                subscribeToPushNotifications();
            } else {
                SimpleDialogFragment.newInstanceNegative(R.string.error, R.string.something_went_wrong, v -> {
                    //todo should we clear database here for don't get realm exception when we will want open it?
//                    startActivity(new Intent(this, SplashActivity.class).putExtra(SplashActivity.RESET_FLAG, true));
                }).show(getSupportFragmentManager(), "");
            }
            updateAssets();
        });
    }

    public void showAssetsScreen() {
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null) {
            tab.select();
        }
    }

    public void showScanScreen() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST_CODE);
        } else {
            startActivityForResult(new Intent(this, ScanActivity.class), Constants.CAMERA_REQUEST_CODE);
        }
    }

    @OnClick(R.id.fast_operations)
    void onFastOperationsClick(final View v) {
        Analytics.getInstance(this).logMain(AnalyticsConstants.MAIN_FAST_OPERATIONS);
        startActivity(new Intent(this, MagicSendActivity.class));
//        v.setEnabled(false);
//        v.postDelayed(() -> v.setEnabled(true), AnimationUtils.DURATION_MEDIUM * 2);
//        Fragment fastOperationsFragment = getSupportFragmentManager().findFragmentByTag(FastOperationsFragment.TAG);
//
//        if (fastOperationsFragment == null) {
//            fastOperationsFragment = FastOperationsFragment.newInstance(
//                    (int) buttonOperations.getX() + buttonOperations.getWidth() / 2,
//                    (int) buttonOperations.getY() + buttonOperations.getHeight() / 2);
//        }
//
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.full_container, fastOperationsFragment, FastOperationsFragment.TAG)
//                .addToBackStack(FastOperationsFragment.TAG)
//                .commit();
    }
}
