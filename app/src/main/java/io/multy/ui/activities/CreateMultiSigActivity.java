/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.guilhe.circularprogressview.CircularProgressView;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.BlueSocketManager;
import io.multy.model.entities.ExchangeRequestEntity;
import io.multy.model.entities.wallet.MultisigEvent;
import io.multy.model.entities.wallet.Owner;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.OwnersAdapter;
import io.multy.ui.fragments.ScanInvitationCodeFragment;
import io.multy.ui.fragments.ShareMultisigFragment;
import io.multy.ui.fragments.main.AssetsFragment;
import io.multy.ui.fragments.main.contacts.ContactInfoFragment;
import io.multy.util.Constants;
import io.realm.RealmList;
import io.socket.client.Ack;
import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class CreateMultiSigActivity extends BaseActivity {

    @BindView(R.id.progress)
    CircularProgressView circularProgressView;
    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.text_status)
    TextView textStatus;
    @BindView(R.id.text_action)
    TextView textAction;
    @BindView(R.id.image_action)
    ImageView imageAction;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.text_count)
    TextView textCount;

    private BlueSocketManager socketManager;

    private static final int SOCKET_JOIN = 1;
    private static final int SOCKET_LEAVE = 2;
    private static final int SOCKET_DELETE = 3; //only for creator delete wallet and leave room
    private static final int SOCKET_KICK = 4; //only creator can kick guys
    public static final int SOCKET_VALIDATE = 5;

    private String endpoint;
    private String inviteCode;
    private String userId;
    private Socket socket;
    private boolean isCreator;

    private Wallet connectedWallet; //TODO make new viewmodel
    private Wallet wallet;

    private OwnersAdapter ownersAdapter;

    // userid address invitecoede --- leave

//    "{          ""userid"": UserID, join payload
//            ""address"": ""Address"",  //of wallet with join
//            ""invitecode"":""kek-string1"",
//            ""addresstokik"":"""",                //only for creator not empty
//            ""walletindex"":0,
//            ""currencyid"":60,
//            ""networkid"":1         }"

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_multisig);
        ButterKnife.bind(this);

        if (socket == null) {
            socketManager = new BlueSocketManager();
        }

        userId = RealmManager.getSettingsDao().getUserId().getUserId();
        endpoint = "message:receive:" + userId;

        socketManager.connect();
        socket = socketManager.getSocket();
        socket.on(endpoint, args -> {
            Timber.i("message received");
            Timber.v("message: " + args[0].toString());
            show(args[0].toString());
        });

        if (getIntent().getBooleanExtra(Constants.EXTRA_SCAN, false)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.root, ScanInvitationCodeFragment.newInstance())
                    .commit();
        } else if (getIntent().getBooleanExtra(Constants.EXTRA_CREATE, false)) {
            inviteCode = getIntent().getStringExtra(Constants.EXTRA_INVITE_CODE);
//            final long walletId = getIntent().getExtras().getLong(Constants.EXTRA_WALLET_ID);
            final long connectedWalletId = getIntent().getExtras().getLong(Constants.EXTRA_RELATED_WALLET_ID);
//            wallet = RealmManager.getAssetsDao().getWalletById(walletId);
            connectedWallet = RealmManager.getAssetsDao().getWalletById(connectedWalletId);
            updateInfo();
        }

        initList();
    }

    private void initList() {
        ownersAdapter = new OwnersAdapter(new RealmList<>());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(ownersAdapter);
    }

    private void fillViews() {
        textCount.setText(wallet.getMultisigWallet().getConfirmations() + " / " + wallet.getMultisigWallet().getOwnersCount());
        ownersAdapter.setOwners(wallet.getMultisigWallet().getOwners());
    }

    public void updateInfo() {
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(Call<WalletsResponse> call, Response<WalletsResponse> response) {
                List<Wallet> wallets = response.body().getWallets();
                for (Wallet wallet : wallets) {
                    if (wallet.getMultisigWallet() != null && wallet.getMultisigWallet().getInviteCode().equals(inviteCode)) {
                        CreateMultiSigActivity.this.wallet = wallet;
                        break;
                    }
                }

                if (wallet != null) {
                    fillViews();
                }
            }

            @Override
            public void onFailure(Call<WalletsResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public void setConnectedWallet(Wallet wallet) {
        this.connectedWallet = wallet;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public Socket getSocket() {
        return socket;
    }

    private void showOwners(List<Owner> owners) {
        for (Owner owner : wallet.getMultisigWallet().getOwners()) {
            if (owner.isCreator() && owner.getUserId().equals(userId)) {
                isCreator = true;
                break;
            }
        }


    }

//    long id = getIntent().getExtras().getLong(Constants.EXTRA_WALLET_ID);
//    Wallet wallet = RealmManager.getAssetsDao().getWalletById(id);
//
//        if (wallet == null) {
//        //TODO do one wallet verbose
//    }
//
//        for (Owner owner : wallet.getMultisigWallet().getOwners()) {
//        if (owner.isCreator() && owner.getUserId() == userId) {
//            isCreator = true;
//            break;
//        }
//    }
//
//    inviteCode = wallet.getMultisigWallet().getInviteCode();
//    endpoint = "message:receive:" + RealmManager.getSettingsDao().getUserId().getUserId();
//    userId = RealmManager.getSettingsDao().getUserId().getUserId();
//
//    socketManager = new BlueSocketManager();
//        socketManager.connect();
//    socket = socketManager.getSocket();
//
//        socket.on(endpoint, args -> {
//        Timber.i("message received");
//        Timber.v("message: " + args[0].toString());
//        show(args[0].toString());
//    });
//
//        if (!isCreator) {
//        join();
//    }

    private void deleteUser() {

    }

    private void show(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void validateCode() {
        MultisigEvent.Payload payload = new MultisigEvent.Payload();
        payload.userId = userId;
        payload.inviteCode = inviteCode;

        //todo fill payload
        try {
            socket.emit("kek", new JSONObject(new Gson().toJson(new MultisigEvent(SOCKET_VALIDATE, System.currentTimeMillis(), payload))), (Ack) args -> {
                Timber.i("Validate code " + inviteCode);
                Timber.d("Validate code: " + args[0].toString());
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void leave() {

    }

    private void join() {
        final Wallet relatedWallet = RealmManager.getAssetsDao().getWalletById(getIntent().getExtras().getLong(Constants.EXTRA_RELATED_WALLET_ID));
        MultisigEvent.Payload payload = new MultisigEvent.Payload();
        payload.address = relatedWallet.getActiveAddress().getAddress();
        payload.inviteCode = inviteCode;
        payload.walletIndex = relatedWallet.getIndex();
        payload.currencyId = relatedWallet.getCurrencyId();
        payload.networkId = relatedWallet.getNetworkId();

        final MultisigEvent event = new MultisigEvent(SOCKET_JOIN, System.currentTimeMillis(), payload);

        try {
            socket.emit(endpoint, new JSONObject(new Gson().toJson(event)), new Ack() {
                @Override
                public void call(Object... args) {
                    Timber.i("JOIN ack");
                    Timber.v("JOIN: " + args[0].toString());
                    show("JOIN: " + args[0].toString());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.button_action)
    public void onClickAction() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.root, ShareMultisigFragment.newInstance(inviteCode))
                .addToBackStack("")
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}
