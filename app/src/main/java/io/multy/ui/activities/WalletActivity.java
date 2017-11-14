/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;

public class WalletActivity extends BaseActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_asset_info);
        ButterKnife.bind(this);
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
}
