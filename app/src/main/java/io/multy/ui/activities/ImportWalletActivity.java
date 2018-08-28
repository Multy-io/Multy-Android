/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.responses.AccountsResponse;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImportWalletActivity extends BaseActivity {

    @BindView(R.id.input_key)
    TextInputEditText inputKey;

    Dialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_continue)
    public void onClickContinue() {
        final int currencyId = NativeDataHelper.Blockchain.EOS.getValue();
        final int networkId = NativeDataHelper.NetworkId.TEST_NET.getValue();
        final String publicKey = NativeDataHelper.getPublicKey(currencyId, networkId, inputKey.getText().toString());

        if (!publicKey.equals("")) {
            showProgressDialog();

            MultyApi.INSTANCE.getAccounts(currencyId, networkId, publicKey).enqueue(new Callback<AccountsResponse>() {
                @Override
                public void onResponse(Call<AccountsResponse> call, Response<AccountsResponse> response) {
                    final int responseCode = response.body().getCode();
                    if (responseCode == 200) {
                        showAccounts(response.body().getAccounts());
                    } else {
                        Toast.makeText(ImportWalletActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<AccountsResponse> call, Throwable t) {
                    Toast.makeText(ImportWalletActivity.this, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            });
        } else {
            Toast.makeText(this, R.string.wrong_private_key, Toast.LENGTH_LONG).show();
            hideProgressDialog();
        }
    }

    private void showAccounts(ArrayList<String> accounts) {
        Intent intent = new Intent(this, FoundWalletsActivity.class);
        intent.putStringArrayListExtra(Constants.EXTRA_ACCOUNTS, accounts);
        startActivity(intent);
        finish();
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new Dialog(this);
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setContentView(R.layout.dialog_spinner);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        } else {
            progressDialog.show();
        }
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.cancel();
        }
    }
}
