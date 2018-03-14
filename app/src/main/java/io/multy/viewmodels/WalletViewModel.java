/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.app.Activity;
import android.app.PendingIntent;
import android.arch.lifecycle.MutableLiveData;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.CurrenciesRate;
import io.multy.api.socket.SocketManager;
import io.multy.api.socket.TransactionUpdateEntity;
import io.multy.model.DataManager;
import io.multy.model.entities.TransactionHistory;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.requests.UpdateWalletNameRequest;
import io.multy.model.responses.ServerConfigResponse;
import io.multy.model.responses.TransactionHistoryResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.asset.AssetInfoFragment;
import io.multy.util.Constants;
import io.multy.util.FirstLaunchHelper;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.util.SingleLiveEvent;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.realm.RealmList;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Intent.ACTION_SEND;

public class WalletViewModel extends BaseViewModel {

    public MutableLiveData<WalletRealmObject> wallet = new MutableLiveData<>();
    public MutableLiveData<String> chainCurrency = new MutableLiveData<>();
    public MutableLiveData<String> fiatCurrency = new MutableLiveData<>();
    private MutableLiveData<Boolean> isWalletUpdated = new MutableLiveData<>();
    public MutableLiveData<List<WalletAddress>> addresses = new MutableLiveData<>();
    public MutableLiveData<Boolean> isRemoved = new MutableLiveData<>();
    public MutableLiveData<CurrenciesRate> rates = new MutableLiveData<>();
    public MutableLiveData<ArrayList<TransactionHistory>> transactions = new MutableLiveData<>();
    public SingleLiveEvent<TransactionUpdateEntity> transactionUpdate = new SingleLiveEvent<>();

    private SocketManager socketManager;

    public WalletViewModel() {
    }

    public void subscribeSocketsUpdate() {
        socketManager = new SocketManager();
        socketManager.connect(rates, transactionUpdate);
    }

    public void unsubscribeSocketsUpdate() {
        if (socketManager != null) {
            socketManager.disconnect();
        }
    }

    public MutableLiveData<List<WalletAddress>> getAddresses() {
        return addresses;
    }

    public WalletRealmObject getWallet(int index) {
        WalletRealmObject wallet = RealmManager.getAssetsDao().getWalletById(index);
        this.wallet.setValue(wallet);
        return wallet;
    }

    public MutableLiveData<WalletRealmObject> getWalletLive() {
        return wallet;
    }

    public WalletRealmObject createWallet(String walletName) {
        isLoading.setValue(true);
        WalletRealmObject walletRealmObject = null;
        try {
            if (!Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
                Multy.makeInitialized();
                FirstLaunchHelper.setCredentials("");
                ServerConfigResponse serverConfig = EventBus.getDefault().removeStickyEvent(ServerConfigResponse.class);
                if (serverConfig != null) {
                    RealmManager.open(Multy.getContext());
                    RealmManager.getSettingsDao().saveDonation(serverConfig.getDonates());
                }
            }
            DataManager dataManager = DataManager.getInstance();

            final int topIndex = Prefs.getInt(Constants.PREF_WALLET_TOP_INDEX, 0);

//            if (!Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
//                FirstLaunchHelper.setCredentials("");
//            }

            String creationAddress = NativeDataHelper.makeAccountAddress(dataManager.getSeed().getSeed(),
                    topIndex, 0, NativeDataHelper.Blockchain.BLOCKCHAIN_BITCOIN.getValue(),
                    NativeDataHelper.BlockchainNetType.BLOCKCHAIN_NET_TYPE_TESTNET.getValue());

            walletRealmObject = new WalletRealmObject();
            walletRealmObject.setName(walletName);

            RealmList<WalletAddress> addresses = new RealmList<>();
            addresses.add(new WalletAddress(0, creationAddress));

            walletRealmObject.setAddresses(addresses);
            walletRealmObject.setCurrency(0);
            walletRealmObject.setAddressIndex(0);
            walletRealmObject.setCreationAddress(creationAddress);
            walletRealmObject.setWalletIndex(topIndex);
        } catch (JniException e) {
            e.printStackTrace();
            isLoading.setValue(false);
            errorMessage.setValue(e.getLocalizedMessage());
            errorMessage.call();
        }
        return walletRealmObject;
    }

    public MutableLiveData<ArrayList<TransactionHistory>> getTransactionsHistory() {
        MultyApi.INSTANCE.getTransactionHistory(wallet.getValue().getCurrency(), wallet.getValue().getWalletIndex()).enqueue(new Callback<TransactionHistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<TransactionHistoryResponse> call, @NonNull Response<TransactionHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transactions.setValue(response.body().getHistories());
                }
            }

            @Override
            public void onFailure(Call<TransactionHistoryResponse> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return transactions;
    }

    public MutableLiveData<Boolean> updateWalletSetting(String newName) {
        int id = wallet.getValue().getWalletIndex();
        int currencyId = wallet.getValue().getCurrency();
        UpdateWalletNameRequest updateWalletName = new UpdateWalletNameRequest(newName, currencyId, id);
        MultyApi.INSTANCE.updateWalletName(updateWalletName).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    RealmManager.getAssetsDao().updateWalletName(id, newName);
                    wallet.setValue(RealmManager.getAssetsDao().getWalletById(wallet.getValue().getWalletIndex()));
                    isWalletUpdated.postValue(true);
                } else {
                    if (response.message() != null) {
                        errorMessage.setValue(response.message());
                    }
                    isWalletUpdated.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                isWalletUpdated.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
        return isWalletUpdated;
    }

    public MutableLiveData<Boolean> removeWallet() {
        isLoading.setValue(true);
        MultyApi.INSTANCE.removeWallet(0, wallet.getValue().getWalletIndex()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                RealmManager.getAssetsDao().removeWallet(wallet.getValue().getWalletIndex());
                isLoading.setValue(false);
                isRemoved.setValue(true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                throwable.printStackTrace();
                isLoading.setValue(false);
                errorMessage.setValue(throwable.getMessage());
            }
        });
        return isRemoved;
    }

    public int getChainId(){
        return 1;
    }

    public void share(Activity activity, String stringToShare) {
        Intent sharingIntent = new Intent(ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, stringToShare);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Intent intentReceiver = new Intent(activity, AssetInfoFragment.SharingBroadcastReceiver.class);
            intentReceiver.putExtra(activity.getString(R.string.chain_id), getChainId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, intentReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
            activity.startActivity(Intent.createChooser(sharingIntent, activity.getResources().getString(R.string.share), pendingIntent.getIntentSender()));
        } else {
            activity.startActivity(Intent.createChooser(sharingIntent, activity.getResources().getString(R.string.share)));
        }
    }

    public void copyToClipboard(Activity activity, String stringToShare) {
        Analytics.getInstance(activity).logWallet(AnalyticsConstants.WALLET_ADDRESS, getChainId());
        String address = stringToShare;
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(address, address);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(activity, R.string.address_copied, Toast.LENGTH_SHORT).show();
    }
}
