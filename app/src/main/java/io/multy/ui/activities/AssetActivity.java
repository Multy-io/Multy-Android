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

import butterknife.BindInt;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.fragments.asset.AssetInfoFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.AssetsViewModel;
import io.multy.viewmodels.WalletViewModel;

public class AssetActivity extends BaseActivity {

    @BindInt(R.integer.one)
    int one;
    @BindInt(R.integer.one_negative)
    int oneNegative;

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
        ViewModelProviders.of(this).get(WalletViewModel.class).destroy();
        super.onDestroy();
    }

    @OnClick(R.id.send)
    void onClickSend() {
        WalletRealmObject wallet = ViewModelProviders.of(this)
                .get(WalletViewModel.class).getWallet(getIntent()
                .getIntExtra(Constants.EXTRA_WALLET_ID, oneNegative));

        String address;
        if (wallet.getAddresses().size() > one) {
            address = wallet.getAddresses().get(wallet.getAddresses().size() - one).getAddress();
        } else {
            address = wallet.getCreationAddress();
        }

        startActivity(new Intent(this, AssetSendActivity.class)
                .addCategory(Constants.EXTRA_SENDER_ADDRESS)
                .putExtra(Constants.EXTRA_WALLET_ID, getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, oneNegative)));
    }

    @OnClick(R.id.receive)
    void onClickReceive() {
        startActivity(new Intent(this, AssetRequestActivity.class)
                .putExtra(Constants.EXTRA_WALLET_ID, getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, oneNegative)));
    }

    @OnClick(R.id.exchange)
    void onClickExchange() {
//        startActivity(new Intent(this, AssetSendActivity.class));
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
