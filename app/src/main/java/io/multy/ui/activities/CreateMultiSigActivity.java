/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.guilhe.circularprogressview.CircularProgressView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.BlueSocketManager;
import io.multy.model.entities.Estimation;
import io.multy.model.entities.ExchangeRequestEntity;
import io.multy.model.entities.wallet.MultisigEvent;
import io.multy.model.entities.wallet.Owner;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.OwnersAdapter;
import io.multy.ui.fragments.ScanInvitationCodeFragment;
import io.multy.ui.fragments.ShareMultisigFragment;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
import io.multy.ui.fragments.main.AssetsFragment;
import io.multy.ui.fragments.main.contacts.ContactInfoFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NativeDataHelper;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.socket.client.Ack;
import io.socket.client.Socket;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static io.multy.ui.fragments.send.SendSummaryFragment.byteArrayToHex;

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
    @BindView(R.id.text_owners)
    TextView textOwners;
    @BindView(R.id.container_header)
    View header;

    private BlueSocketManager socketManager;

    private static final int SOCKET_JOIN = 1;
    private static final int SOCKET_LEAVE = 2;
    private static final int SOCKET_DELETE = 3; //only for creator delete wallet and leave room
    private static final int SOCKET_KICK = 4; //only creator can kick guys
    public static final int SOCKET_VALIDATE = 5;

    private String endpointReceive;
    private String endpointSend = "message:send";
    private String inviteCode;
    private String userId;
    private Socket socket;
    private boolean isCreator;

    private Wallet connectedWallet; //TODO make new viewmodel
    private Wallet wallet;

    private OwnersAdapter ownersAdapter;

    // userid address invitecoede --- leave

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_multisig);
        ButterKnife.bind(this);

        if (socket == null) {
            socketManager = new BlueSocketManager();
        }

        userId = RealmManager.getSettingsDao().getUserId().getUserId();
        endpointReceive = "message:receive:" + userId;

        socketManager.connect();
        socket = socketManager.getSocket();
        socket.on(endpointReceive, args -> {
            Timber.i("message received");
            Timber.v("message: " + args[0].toString());
            updateInfo();
        });

        if (getIntent().getBooleanExtra(Constants.EXTRA_SCAN, false)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.root, ScanInvitationCodeFragment.newInstance())
                    .commit();
        } else if (getIntent().getBooleanExtra(Constants.EXTRA_CREATE, false)) {
            inviteCode = getIntent().getStringExtra(Constants.EXTRA_INVITE_CODE);
            final long walletId = getIntent().getExtras().getLong(Constants.EXTRA_WALLET_ID);
            wallet = RealmManager.getAssetsDao().getWalletById(walletId);
            findConnectedWallet();
            updateInfo();
        }

        initList();
    }

    private void findConnectedWallet() {
        if (wallet != null) {
            RealmResults<Wallet> wallets = RealmManager.getAssetsDao().getWallets(wallet.getCurrencyId(), wallet.getNetworkId());

            //multisig parent wallet derivation paths are equal to connected eth wallet. so we need to find wallet
            //with the same derivation paths but without multisig and owners entities
            for (Wallet wallet : wallets) {
                if (wallet.getMultisigWallet() == null) {
                    this.connectedWallet = wallet;
                    break;
                }
            }
        }
    }

    private void initList() {
        ownersAdapter = new OwnersAdapter(v -> {
            showKickDialog((String) v.getTag());
            return true;
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(ownersAdapter);
    }

    private void showKickDialog(String addressToKick) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_user)
                .setPositiveButton(R.string.yes, (dialog, which) -> kickUser(addressToKick))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel())
                .show();
    }

    private void fillViews() {
        textCount.setText(wallet.getMultisigWallet().getOwners().size() + " / " + wallet.getMultisigWallet().getOwnersCount());

        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Owner> owners = new ArrayList<>(wallet.getMultisigWallet().getOwnersCount());
        Owner owner;
        for (int i = 0; i < wallet.getMultisigWallet().getOwnersCount(); i++) {
            if (i < wallet.getMultisigWallet().getOwners().size()) {
                owner = wallet.getMultisigWallet().getOwners().get(i);
                stringBuilder.append(owner.getAddress());
                if (i != wallet.getMultisigWallet().getOwnersCount() - 1) {
                    stringBuilder.append("\n");
                }
            } else {
                owner = null;
            }
            owners.add(owner);
        }

        ownersAdapter.setOwners(owners);
        textOwners.setText(stringBuilder.toString());

        if (wallet.getMultisigWallet().getOwners().size() == wallet.getMultisigWallet().getOwnersCount()) {
            MultyApi.INSTANCE.getEstimations("price").enqueue(new Callback<Estimation>() {
                @Override
                public void onResponse(Call<Estimation> call, Response<Estimation> response) {
                    final String deployWei = response.body().getPriceOfCreation();
                    final String deployEth = CryptoFormatUtils.weiToEthLabel(deployWei);
                    textAction.setText("START FOR " + deployEth);
                    textAction.setTag(deployWei);
                }

                @Override
                public void onFailure(Call<Estimation> call, Throwable t) {
                    t.printStackTrace();
                }
            });
            imageAction.setVisibility(View.GONE);
        } else {
            imageAction.setVisibility(View.VISIBLE);
            textAction.setText(R.string.invitation_code);
        }
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

    private void validateCode() {
        MultisigEvent.Payload payload = new MultisigEvent.Payload();
        payload.userId = userId;
        payload.inviteCode = inviteCode;

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

    public void join() {
        MultisigEvent.Payload payload = new MultisigEvent.Payload();
        payload.address = connectedWallet.getActiveAddress().getAddress();
        payload.inviteCode = inviteCode;
        payload.walletIndex = connectedWallet.getIndex();
        payload.currencyId = connectedWallet.getCurrencyId();
        payload.networkId = connectedWallet.getNetworkId();
        payload.userId = userId;

        final MultisigEvent event = new MultisigEvent(SOCKET_JOIN, System.currentTimeMillis(), payload);

        try {
            socket.emit(endpointSend, new JSONObject(new Gson().toJson(event)), (Ack) args -> {
                Timber.i("JOIN ack");
                Timber.v("JOIN: " + args[0].toString());
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void kickUser(String addressToKick) {
        MultisigEvent.Payload payload = new MultisigEvent.Payload();
        payload.address = connectedWallet.getActiveAddress().getAddress();
        payload.inviteCode = inviteCode;
        payload.walletIndex = connectedWallet.getIndex();
        payload.currencyId = connectedWallet.getCurrencyId();
        payload.networkId = connectedWallet.getNetworkId();
        payload.userId = userId;
        payload.addressToKick = addressToKick;

        final MultisigEvent event = new MultisigEvent(SOCKET_KICK, System.currentTimeMillis(), payload);

        try {
            final JSONObject jsonObject = new JSONObject(new Gson().toJson(event));
            Timber.i("KICK - " + jsonObject.toString());
            socket.emit(endpointSend, jsonObject, (Ack) args -> {
                updateInfo();
                Timber.v("KICK: " + args[0].toString());
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.button_action)
    public void onClickAction() {
        if (imageAction.getVisibility() == View.VISIBLE) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.root, ShareMultisigFragment.newInstance(inviteCode))
                    .addToBackStack("")
                    .commit();

        } else {
            final String priceOfCreation = (String) textAction.getTag();
            final byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();
            final String factoryAddress = RealmManager.getSettingsDao().getMultisigFactory().getEthTestNet();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            for (Owner owner : wallet.getMultisigWallet().getOwners()) {
                stringBuilder.append(owner.getAddress());
                stringBuilder.append(", ");
            }
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            stringBuilder.append("]");

            final byte[] transaction = NativeDataHelper.createEthMultisigWallet(seed, connectedWallet.getIndex(), connectedWallet.getActiveAddress().getIndex(), connectedWallet.getCurrencyId(), connectedWallet.getNetworkId(),
                    connectedWallet.getActiveAddress().getAmountString(), "5000000", "3000000000", connectedWallet.getEthWallet().getNonce(), factoryAddress,
                    stringBuilder.toString(), wallet.getMultisigWallet().getConfirmations(), priceOfCreation);
            String hex = "0x" + byteArrayToHex(transaction);

            final HdTransactionRequestEntity entity = new HdTransactionRequestEntity(wallet.getCurrencyId(), wallet.getNetworkId(),
                    new HdTransactionRequestEntity.Payload("", wallet.getAddresses().size(),
                            wallet.getIndex(), hex, false));

            Timber.i("hex=%s", hex);
            MultyApi.INSTANCE.sendHdTransaction(entity).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        CompleteDialogFragment.newInstance(wallet.getCurrencyId()).show(getSupportFragmentManager(), "");
                    } else {
//                        showError(null);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    t.printStackTrace();
//                    showError((Exception) t);
                }
            });
        }
    }

    private void showError() {
        new AlertDialog.Builder(this);
    }

    @OnClick(R.id.button_settings)
    public void onClickSettings() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}
