/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.fragments.asset.AssetInfoFragment;
import io.multy.viewmodels.AssetsViewModel;

public class AssetActivity extends BaseActivity {

    private boolean isFirstFragmentCreation;

    private WalletRealmObject walletRealmObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_info);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;

//        walletRealmObject = new DataManager(this).getWalletLive(getIntent().getExtras().getInt(Constants.EXTRA_WALLET_ID, 0));

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setFragment(R.id.frame_container, AssetInfoFragment.newInstance());
    }

    @Override
    protected void onDestroy() {
        ViewModelProviders.of(this).get(AssetsViewModel.class).destroy();
        super.onDestroy();
    }

    @OnClick(R.id.send)
    void onClickSend() {
        startActivity(new Intent(this, AssetSendActivity.class));
    }

    @OnClick(R.id.receive)
    void onClickReceive() {
        startActivity(new Intent(this, AssetRequestActivity.class));
    }

    @OnClick(R.id.exchange)
    void onClickExchange() {
        startActivity(new Intent(this, AssetSendActivity.class));
    }

    public WalletRealmObject getWalletRealmObject() {
        return walletRealmObject;
    }

    public void setFragment(@IdRes int container, Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(container, fragment);

        if (!isFirstFragmentCreation) {
            transaction.addToBackStack(fragment.getClass().getName());
        }

        isFirstFragmentCreation = false;

        transaction.commit();
    }
}
