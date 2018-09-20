/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;

public class ImportEthWalletActivity extends BaseActivity {

    @BindView(R.id.input_key)
    EditText inputKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_eth);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.text_cancel)
    public void onClickCancel() {
        finish();
    }

    @OnClick(R.id.button_import)
    public void onClickImport() {
        final String key = inputKey.getText().toString();
        //TODO API CALL IMPORT
    }
}
