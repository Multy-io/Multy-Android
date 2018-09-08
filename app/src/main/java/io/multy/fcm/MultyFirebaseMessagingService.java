/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.samwolfand.oneprefs.Prefs;

import net.khirr.library.foreground.Foreground;

import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.responses.SingleWalletResponse;
import io.multy.storage.AssetsDao;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.activities.MainActivity;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MultyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MultyFirebaseMessagingService.class.getSimpleName();
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_CURRENCY_ID = "currencyid";
    private static final String KEY_NETWORK_ID = "networkid";
    private static final String KEY_WALLET_INDEX = "walletindex";
    private static final String KEY_TX_HASH = "txid";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (Prefs.getBoolean(Constants.PREF_IS_PUSH_ENABLED, true) && remoteMessage.getData().size() > 0) {
            if (Foreground.Companion.isBackground()) {
                try {
                    final int currencyId = Integer.parseInt(remoteMessage.getData().get(KEY_CURRENCY_ID));
                    final int networkId = Integer.parseInt(remoteMessage.getData().get(KEY_NETWORK_ID));
                    final int walletIndex = Integer.parseInt(remoteMessage.getData().get(KEY_WALLET_INDEX));
                    MultyApi.INSTANCE.getWalletVerbose(walletIndex, currencyId, networkId, Constants.ASSET_TYPE_ADDRESS_MULTY)
                            .enqueue(new Callback<SingleWalletResponse>() {
                        @Override
                        public void onResponse(Call<SingleWalletResponse> call, Response<SingleWalletResponse> response) {
                            Realm realm = Realm.getInstance(Multy.getRealmConfiguration());
                            try {
                                if (response.isSuccessful() && response.body().getWallets() != null) {
                                    AssetsDao dao = new AssetsDao(realm);
                                    dao.saveWallet(response.body().getWallets().get(0));
                                    String amount = "";
                                    switch (NativeDataHelper.Blockchain.valueOf(currencyId)) {
                                        case BTC:
                                            amount = CryptoFormatUtils.satoshiToBtcLabel(Long.parseLong(remoteMessage.getData().get(KEY_AMOUNT)));
                                            break;
                                        case ETH:
                                            amount = CryptoFormatUtils.weiToEthLabel(remoteMessage.getData().get(KEY_AMOUNT));
                                            break;
                                    }
                                    final String notification = String.format(getString(R.string.push_new_transaction_notification), amount);
                                    Wallet wallet = dao.getWallet(currencyId, networkId, walletIndex);
                                    final String txHash = remoteMessage.getData().get(KEY_TX_HASH);
                                    sendNotification(notification, wallet.getId(), txHash);
                                } else {
                                    Log.e(TAG, "get wallet verbose error. walletId = " + String.valueOf(walletIndex) +
                                            ", currencyId = " + String.valueOf(currencyId) +
                                            ", networkId = " + String.valueOf(networkId));
                                }
                            } finally {
                                realm.close();
                            }
                        }

                        @Override
                        public void onFailure(Call<SingleWalletResponse> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                    Analytics.getInstance(this).logPush(AnalyticsConstants.PUSH_RECEIVED, remoteMessage.getMessageId());
//                EventBus.getDefault().post(new TransactionUpdateEvent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            }
        }
    }

    private void sendNotification(String messageBody, long walletId, String txHash) {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Intent assetIntent = new Intent(this, AssetActivity.class);
        assetIntent.putExtra(Constants.EXTRA_WALLET_ID, walletId);
        Log.i(TAG, "wallet id = " + String.valueOf(walletId));
        assetIntent.putExtra(Constants.EXTRA_TX_HASH, txHash);
        Intent[] intents = new Intent[]{mainIntent, assetIntent};
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0 /* Request code */, intents, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this, getString(R.string.channel_id))
                        .setSmallIcon(R.drawable.ic_multy)
                        .setColor(getResources().getColor(R.color.colorPrimaryDark))
                        .setContentTitle(getString(R.string.you_have_transaction))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setContentText(messageBody);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id), "Multy.io", NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }
        mNotificationManager.notify(1, notification.build());
    }
}
