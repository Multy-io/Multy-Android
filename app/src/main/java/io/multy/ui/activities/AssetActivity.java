/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.fragments.asset.AssetInfoFragment;
import io.multy.util.Constants;

public class AssetActivity extends BaseActivity {

    private WalletRealmObject walletRealmObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_info);
        ButterKnife.bind(this);

        walletRealmObject = new DataManager(this).getWallet(getIntent().getExtras().getInt(Constants.EXTRA_WALLET_ID, 0));

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(AssetInfoFragment.TAG);
        if (fragment == null) {
            fragment = AssetInfoFragment.newInstance();
        }
        manager.beginTransaction()
                .replace(R.id.frame_container, fragment, AssetInfoFragment.TAG)
                .commit();
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
}
