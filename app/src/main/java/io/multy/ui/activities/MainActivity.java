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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.scottyab.rootbeer.RootBeer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.branch.referral.Branch;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.ui.fragments.main.AssetsFragment;
import io.multy.ui.fragments.main.ContactsFragment;
import io.multy.ui.fragments.main.FeedFragment;
import io.multy.ui.fragments.main.SettingsFragment;
import io.multy.util.Constants;
import io.multy.util.SocketHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    private boolean isFirstFragmentCreation;
    private int lastTabPosition = 0;
    private SocketHelper socketHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;
        setupFooter();
        setFragment(R.id.container_frame, AssetsFragment.newInstance());

        socketHelper = new SocketHelper();

//        preventRootIfDetected();

//        MultyApi.INSTANCE.getWalletAddresses(0)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<UserAssetsResponse>() {
//                    @Override
//                    public void accept(UserAssetsResponse userAssetsResponse) throws Exception {
//                        Log.i("wise", "accept");
//                    }
//                });

//        Call<ResponseBody> responseBodyCall = MultyApi.INSTANCE.getSpendableOutputs(0);
//        responseBodyCall.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Log.i("wise", "onResponse ");
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });
    }

    private void preventRootIfDetected() {
        RootBeer rootBeer = new RootBeer(this);
        if (rootBeer.isRootedWithoutBusyBoxCheck()) {
            SimpleDialogFragment.newInstanceNegative(R.string.root_title, R.string.root_message, view -> finish())
                    .show(getSupportFragmentManager(), "");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBranchIO();
    }

    private void initBranchIO() {
        Branch branch = Branch.getInstance(getApplicationContext());

        branch.initSession((referringParams, error) -> {
            Log.i(getClass().getSimpleName(), "branch io link");
            if (error == null) {
                String qrCode = referringParams.optString(Constants.DEEP_LINK_QR_CODE);
                if (!TextUtils.isEmpty(qrCode)) {
                    Log.i(getClass().getSimpleName(), "branch io link exist");
                    getIntent().putExtra(Constants.DEEP_LINK_QR_CODE, qrCode);
                }

//                {"session_id":"465086124545736122","identity_id":"465073643500779705","link":"https://zn0o.test-app.link?%24identity_id=465073643500779705",
//                  "data":"{\"$og_title\":\"QR_CODE\",\"$publicly_indexable\":\"true\",\"~creation_source\":2,\"$og_description\":\"Multi cryptocurrency and assets open-source wallet\",
//                  \"+referrer\":\"com.skype.raider\",\"+click_timestamp\":1512122667,\"QR_CODE\":\"bitcoin:1GLY7sDe7a6xsewDdUNA6F8CEoAxQsHV37\",
//                  \"source\":\"android\",\"$identity_id\":\"465073643500779705\",\"$og_image_url\":\"http://multy.io/wp-content/uploads/2017/11/logo-1.png\",
//                  \"~feature\":\"Share\",\"+match_guaranteed\":false,\"$desktop_url\":\"http://multy.io\",\"~tags\":[\"bitcoin:1GLY7sDe7a6xsewDdUNA6F8CEoAxQsHV37\"],
//                  \"$canonical_identifier\":\"QR_CODE/bitcoin:1GLY7sDe7a6xsewDdUNA6F8CEoAxQsHV37\",\"+clicked_branch_link\":true,\"$one_time_use\":false,
//                  \"~id\":\"465075453422808951\",\"+is_first_session\":false,\"~referring_link\":\"https://zn0o.test-app.link/7kshikidwI\"}","device_fingerprint_id":"465073643483986343"}
            } else {
                Log.i(getClass().getSimpleName(), error.getMessage());
            }
        }, this.getIntent().getData(), this);
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
    void onFastOperationsClick() {
//        FirebaseMessaging.getInstance().subscribeToTopic("someTopic");
//        Fragment fastOperationsFragment = getSupportFragmentManager()
//                .findFragmentByTag(FastOperationsFragment.TAG);
//        if (fastOperationsFragment == null) {
//            fastOperationsFragment = FastOperationsFragment.newInstance();
//        }
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.full_container, fastOperationsFragment, FastOperationsFragment.TAG)
//                .addToBackStack(FastOperationsFragment.TAG)
//                .commit();

        MultyApi.INSTANCE.getSpendableOutputs(0).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i("wise", "response");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

//        RealmResults<WalletRealmObject> wallets = new DataManager(this).getWallets();
//        if (wallets != null && wallets.size() > 0) {
//            WalletRealmObject walletRealmObject = wallets.get(0);
//            try {
//                Log.i("wise", "address " + walletRealmObject.getCreationAddress());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        MultyApi.INSTANCE.getSpendableOutputs();
//        MultyApi.INSTANCE.getUserAssets()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<UserAssetsResponse>() {
//                    @Override
//                    public void accept(UserAssetsResponse userAssetsResponse) throws Exception {
//                        Log.i("wise", "response");
//                    }
//                });
//        MultyApi.INSTANCE.getTransactionSpeed();
//        try {
////            Log.i("wise", "to " + (NativeDataHelper.makeAccountAddress(, 0, 0)));
//
//            byte[] seed = new DataManager(this).getSeed().getSeed();
//            final String txHash = "6e26d2fb53983051172a2714838fab9c3241d5dcdb022926b834e254e7ae2034";
//            final String pubKey = "76a9149543b205596749495eae1d6305434c2c8084b25a88ac";
//            final String address = "mu8C5CGtmSn3eUhMFEmU2CvRse7rLKgYUN";
//            byte[] hexTx = NativeDataHelper.makeTransaction(seed, txHash, pubKey, 0, "200000000","150000000", "1000000", "mzqiDnETWkunRDZxjUQ34JzN1LDevh5DpU", address);
//            String hex = byteArrayToHex(hexTx);
//            Log.i("multy", "raw transaction generated successfully " + hex);
//        } catch (JniException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onDestroy() {
        socketHelper.disconnect();
        super.onDestroy();
    }
}
