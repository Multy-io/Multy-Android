/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.ToggleGroup;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import io.multy.R;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.viewmodels.ImportViewModel;

public class ImportMultisigActivity extends BaseActivity {

    @BindView(R.id.input_key)
    EditText inputKey;
    @BindView(R.id.input_address)
    EditText inputAddress;
    private ImportViewModel viewModel;
    @BindView(R.id.group_network)
    ToggleGroup groupNetwork;
    @BindView(R.id.nested)
    NestedScrollView scrollView;

    private int currencyId = NativeDataHelper.Blockchain.ETH.getValue();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_multisig);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this).get(ImportViewModel.class);
    }

    @OnFocusChange({R.id.input_key, R.id.input_address})
    void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            scrollView.post(() -> scrollView.scrollBy(0, (int) (view.getY() - (scrollView.getScrollY() + scrollView.getY()))));
        }
    }

    @OnClick(R.id.text_cancel)
    public void onClickCancel() {
        finish();
    }

    @OnClick(R.id.button_import)
    public void onClickImport(View view) {
        final String key = inputKey.getText().toString();
        final String multisigAddress = inputAddress.getText().toString();
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(multisigAddress)) {
            try {
                final int networkId = groupNetwork.getCheckedId() == R.id.button_main ?
                        NativeDataHelper.NetworkId.ETH_MAIN_NET.getValue() : NativeDataHelper.NetworkId.RINKEBY.getValue();
                final String linkedAddress = NativeDataHelper.makeAccountAddressFromKey(key, currencyId, networkId);
                viewModel.importEthWallet(currencyId, networkId, linkedAddress, isLinkedWallet -> {
                    if (isLinkedWallet) {
                        viewModel.importEthMultisigWallet(currencyId, networkId, linkedAddress, multisigAddress, isMultisigWallet -> {
                            if (isMultisigWallet) {
                                CompleteDialogFragment.newInstance(currencyId)
                                        .show(getSupportFragmentManager(), CompleteDialogFragment.class.getSimpleName());
                            } else {
                                viewModel.errorMessage.setValue(getString(R.string.something_went_wrong));
                            }
                        });
                    } else {
                        viewModel.errorMessage.setValue(getString(R.string.something_went_wrong));
                    }
                });
            } catch (JniException e) {
                e.printStackTrace();
                viewModel.errorMessage.setValue(getString(R.string.something_went_wrong));
            }
        }
    }
}
