/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.ToggleGroup;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
import io.multy.util.NativeDataHelper;
import io.multy.viewmodels.ImportViewModel;

public class ImportEthWalletActivity extends BaseActivity {

    @BindView(R.id.input_key)
    EditText inputKey;
    @BindView(R.id.group_network)
    ToggleGroup groupNetwork;

    private ImportViewModel viewModel;
    private int currencyId = NativeDataHelper.Blockchain.ETH.getValue();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_eth);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this).get(ImportViewModel.class);
    }

    @OnClick(R.id.text_cancel)
    public void onClickCancel() {
        finish();
    }

    @OnClick(R.id.button_import)
    public void onClickImport(View view) {
        if (!TextUtils.isEmpty(inputKey.getText())) {
            try {
                view.setEnabled(false);
                view.postDelayed(() -> view.setEnabled(true), 300);
                final String key = inputKey.getText().toString();
                final int networkId = groupNetwork.getCheckedId() == R.id.button_main ?
                        NativeDataHelper.NetworkId.ETH_MAIN_NET.getValue() : NativeDataHelper.NetworkId.RINKEBY.getValue();
                final String address = NativeDataHelper.makeAccountAddressFromKey(key, currencyId, networkId);
                viewModel.importEthWallet(currencyId, networkId, address, isSuccessful -> {
                    if (!isSuccessful) {
                        viewModel.errorMessage.setValue(getString(R.string.something_went_wrong));
                    } else {
                        CompleteDialogFragment.newInstance(currencyId)
                                .show(getSupportFragmentManager(), CompleteDialogFragment.class.getSimpleName());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                viewModel.errorMessage.setValue(getString(R.string.something_went_wrong));
            }
        }
    }
}
