/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.SocketManager;
import io.multy.model.entities.Estimation;
import io.multy.model.entities.wallet.MultisigEvent;
import io.multy.model.entities.wallet.Owner;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.responses.FeeRateResponse;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.RealmManager;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;
import io.socket.client.Ack;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class CreateMultisigViewModel extends BaseViewModel {

    private boolean isCreator = false;
    private long walletId;
    private SocketManager socketManager;
    private MutableLiveData<Wallet> multisigWallet = new MutableLiveData<>();
    private MutableLiveData<Wallet> linkedWallet = new MutableLiveData<>();
    private MutableLiveData<String> inviteCode = new MutableLiveData<>();
    private String gasLimit = "5000000";
    private String gasPrice = "3000000000";

    public void connectSockets(Emitter.Listener args) {
        try {
            if (socketManager == null) {
                socketManager = new SocketManager();
            }
            final String eventReceive = SocketManager.getEventReceive(RealmManager.getSettingsDao().getUserId().getUserId());
            socketManager.listenEvent(eventReceive, args);
            socketManager.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void disconnectSockets() {
        if (socketManager != null && socketManager.isConnected()) {
            socketManager.disconnect();
        }
    }

    public boolean isCreator() {
        return isCreator;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
        getMultisigWallet();
        checkCreatorStatus();
        if (isCreator) {
            requestEstimation();
        }
    }

    public MutableLiveData<String> getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode.setValue(inviteCode);
    }

    public MutableLiveData<Wallet> getMultisigWallet() {
        if (multisigWallet.getValue() == null || !multisigWallet.getValue().isValid()) {
            if (walletId == 0 && inviteCode.getValue() != null) {
                Wallet wallet = RealmManager.getAssetsDao().getMultisigWallet(inviteCode.getValue());
                if (wallet != null) {
                    walletId = wallet.getId();
                    multisigWallet.setValue(wallet);
                }
            } else if (walletId != 0) {
                multisigWallet.setValue(RealmManager.getAssetsDao().getWalletById(walletId));
            }
        }
        return multisigWallet;
    }

    public MutableLiveData<Wallet> getLinkedWallet() {
        if (linkedWallet.getValue() == null || !linkedWallet.getValue().isValid()) {
            updateLinkedWallet();
        }
        return linkedWallet;
    }

    public void setLinkedWallet(Wallet wallet) {
        linkedWallet.setValue(wallet);
//        updateMultisigWallet();
    }

    public void updateMultisigWallet() {
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletsResponse> call, @NonNull Response<WalletsResponse> response) {
                WalletsResponse body = response.body();
                if (response.isSuccessful() && body != null) {
                    RealmManager.getAssetsDao().saveWallets(body.getWallets());
                    getMultisigWallet();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WalletsResponse> call, @NonNull Throwable t) {
                errorMessage.postValue(t.getLocalizedMessage());
            }
        });
    }

    public void updateLinkedWallet() {
        if (getMultisigWallet().getValue() != null) {
            RealmList<Owner> owners = getMultisigWallet().getValue().getMultisigWallet().getOwners();
            linkedWallet.setValue(RealmManager.getAssetsDao().getMultisigLinkedWallet(owners));
        }
    }

    private void requestEstimation() {
        Wallet wallet = getMultisigWallet().getValue();
        if (wallet != null && wallet.isValid()) {
            Wallet objectWallet = wallet.getRealm().copyFromRealm(wallet);
            isLoading.setValue(true);
            Disposable disposable = Flowable.create((FlowableOnSubscribe<Void>) e -> {
                FeeRateResponse fee = MultyApi.INSTANCE
                        .getFeeRates(objectWallet.getCurrencyId(), objectWallet.getNetworkId(), "price")
                        .execute().body();
                if (fee != null) {
                    gasPrice = String.valueOf(fee.getSpeeds().getFast());
                }
                Estimation estimation = MultyApi.INSTANCE.getEstimations("price").execute().body();
                if (estimation != null) {
                    gasLimit = String.valueOf(estimation.getDeployMultisig());
                }
                e.onComplete();
            }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aVoid -> {}, throwable -> {
                        throwable.printStackTrace();
                        isLoading.setValue(false);
                    }, () -> isLoading.setValue(false));
            addDisposable(disposable);
        }
    }

    private void checkCreatorStatus() {
        if (multisigWallet.getValue() != null && multisigWallet.getValue().isMultisig()) {
            final String userId = RealmManager.getSettingsDao().getUserId().getUserId();
            for (Owner owner : multisigWallet.getValue().getMultisigWallet().getOwners()) {
                if (owner.isCreator() && owner.getUserId().equals(userId)) {
                    isCreator = true;
                    break;
                }
            }
        }
    }

    public void validate(String inviteCode, Ack ack) {
        if (socketManager != null) {
            final String userId = RealmManager.getSettingsDao().getUserId().getUserId();
            MultisigEvent event = MultisigEvent.getBuilder()
                    .setType(SocketManager.SOCKET_VALIDATE)
                    .setDate(System.currentTimeMillis())
                    .setPayload(MultisigEvent.Payload.getBuilder()
                            .setInviteCode(inviteCode)
                            .setUserId(userId)
                            .build())
                    .build();
            try {
                final String eventJson = new Gson().toJson(event);
                Timber.i("sending socket " + eventJson);
                socketManager.sendEvent(SocketManager.EVENT_MESSAGE_SEND, new JSONObject(eventJson), ack);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void join() {
        if (socketManager != null) {
            Wallet linkedWallet = getLinkedWallet().getValue();
            final String userId = RealmManager.getSettingsDao().getUserId().getUserId();
            final MultisigEvent event = MultisigEvent.getBuilder()
                    .setType(SocketManager.SOCKET_JOIN)
                    .setDate(System.currentTimeMillis())
                    .setPayload(MultisigEvent.Payload.getBuilder()
                            .setAddress(linkedWallet.getActiveAddress().getAddress())
                            .setInviteCode(inviteCode.getValue())
                            .setWalletIndex(linkedWallet.getIndex())
                            .setCurrencyId(linkedWallet.getCurrencyId())
                            .setNetworkId(linkedWallet.getNetworkId())
                            .setUserId(userId)
                            .build())
                    .build();
            try {
                final String eventJson = new Gson().toJson(event);
                socketManager.sendEvent(SocketManager.EVENT_MESSAGE_SEND, new JSONObject(eventJson), ack -> {
                    Timber.i("JOIN ack");
                    Timber.v("JOIN: " + ack[0].toString());
                    updateMultisigWallet();
                });
            } catch (JSONException e) {
                e.printStackTrace();
                errorMessage.setValue(Multy.getContext().getString(R.string.something_went_wrong));
            }
        }
    }

    public void kickUser(String addressToKick) {
        if (socketManager != null) {
            Wallet linkedWallet = getLinkedWallet().getValue();
            final String userId = RealmManager.getSettingsDao().getUserId().getUserId();
            final MultisigEvent event = MultisigEvent.getBuilder()
                    .setType(SocketManager.SOCKET_KICK)
                    .setDate(System.currentTimeMillis())
                    .setPayload(MultisigEvent.Payload.getBuilder()
                            .setAddress(linkedWallet.getActiveAddress().getAddress())
                            .setInviteCode(inviteCode.getValue())
                            .setWalletIndex(linkedWallet.getIndex())
                            .setCurrencyId(linkedWallet.getCurrencyId())
                            .setNetworkId(linkedWallet.getNetworkId())
                            .setUserId(userId)
                            .setAddressToKick(addressToKick)
                            .build())
                    .build();
            final String eventJson = new Gson().toJson(event);
            try {
                socketManager.sendEvent(SocketManager.EVENT_MESSAGE_SEND, new JSONObject(eventJson), args -> {
                  updateMultisigWallet();
                  Timber.v("KICK: " + args[0].toString());
                });
            } catch (JSONException e) {
                e.printStackTrace();
                errorMessage.setValue(Multy.getContext().getString(R.string.something_went_wrong));
            }
        }
    }

    public void leaveWallet(Ack ack) {
        if (socketManager != null) {
            Wallet linkedWallet = getLinkedWallet().getValue();
            final String userId = RealmManager.getSettingsDao().getUserId().getUserId();
            final MultisigEvent event = MultisigEvent.getBuilder()
                    .setType(SocketManager.SOCKET_LEAVE)
                    .setDate(System.currentTimeMillis())
                    .setPayload(MultisigEvent.Payload.getBuilder()
                            .setAddress(linkedWallet.getActiveAddress().getAddress())
                            .setInviteCode(inviteCode.getValue())
                            .setWalletIndex(linkedWallet.getIndex())
                            .setCurrencyId(linkedWallet.getCurrencyId())
                            .setNetworkId(linkedWallet.getNetworkId())
                            .setUserId(userId)
                            .build())
                    .build();
            try {
                final String eventJson = new Gson().toJson(event);
                socketManager.sendEvent(SocketManager.EVENT_MESSAGE_SEND, new JSONObject(eventJson), ack);
            } catch (JSONException e) {
                e.printStackTrace();
                errorMessage.setValue(Multy.getContext().getString(R.string.something_went_wrong));
            }
        }
    }

    public void deleteWallet(Ack ack) {
        if (socketManager != null) {
            Wallet linkedWallet = getLinkedWallet().getValue();
            final String userId = RealmManager.getSettingsDao().getUserId().getUserId();
            final MultisigEvent event = MultisigEvent.getBuilder()
                    .setType(SocketManager.SOCKET_DELETE)
                    .setDate(System.currentTimeMillis())
                    .setPayload(MultisigEvent.Payload.getBuilder()
                            .setAddress(linkedWallet.getActiveAddress().getAddress())
                            .setInviteCode(inviteCode.getValue())
                            .setWalletIndex(linkedWallet.getIndex())
                            .setCurrencyId(linkedWallet.getCurrencyId())
                            .setNetworkId(linkedWallet.getNetworkId())
                            .setUserId(userId)
                            .build())
                    .build();
            try {
                final String eventJson = new Gson().toJson(event);
                socketManager.sendEvent(SocketManager.EVENT_MESSAGE_SEND, new JSONObject(eventJson), ack);
            } catch (JSONException e) {
                e.printStackTrace();
                errorMessage.setValue(Multy.getContext().getString(R.string.something_went_wrong));
            }
        }
    }
}
