/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.guilhe.circularprogressview.CircularProgressView;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.BlueSocketManager;
import io.multy.model.entities.wallet.MultisigEvent;
import io.multy.model.entities.wallet.Owner;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.OwnersAdapter;
import io.multy.ui.fragments.ScanInvitationCodeFragment;
import io.multy.ui.fragments.ShareMultisigFragment;
import io.multy.util.Constants;
import io.realm.RealmList;
import io.socket.client.Ack;
import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ImportMultisigActivity extends BaseActivity {

    //for request
//    {
//        "currencyID": 60,
//            "networkID": 4,
//            "address": "0xfad9edb6094fc4909c6f1b236ca4dd77c1165f53",
//            "addressIndex": 0,
//            "walletIndex": 0,
//            "walletName": "imp ms",
//            "isImported": true,
//            "multisig": {
//        "isMultisig": true,
//                "signaturesRequired": 2,
//                "ownersCount": 2,
//                "inviteCode": "",
//                "isImported": true,
//                "contractAddress": "0x39a7873d3827ee26f222E47eb283BF3b7d60e377"
//    }
//    }

    @BindView(R.id.input_key)
    EditText inputKey;

    @BindView(R.id.input_address)
    EditText inputAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_multisig);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.text_cancel)
    public void onClickCancel() {
        finish();
    }

    @OnClick(R.id.button_import)
    public void onClickImport() {
        final String key = inputKey.getText().toString();
        final String address = inputKey.getText().toString();
        //TODO API CALL IMPORT
    }
}
