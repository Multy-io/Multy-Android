/*
 * Copyright 2017 Idealnaya rabota LLC
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

import java.util.Map;

import io.multy.R;
import io.multy.ui.activities.MainActivity;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;

public class MultyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MultyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            try {
                Map<String, String> remoteData = remoteMessage.getData();
                String amount = remoteData.get("amount") + " BTC";
                String type = remoteData.get("transactionType");
                sendNotification("transaction " + type + ".\nAmount = " + amount, remoteMessage.getMessageId());
                Analytics.getInstance(this).logPush(AnalyticsConstants.PUSH_RECEIVED, remoteMessage.getMessageId());
//                EventBus.getDefault().post(new TransactionUpdateEvent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }
    }

    private void sendNotification(String messageBody, String pushId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(getString(R.string.push_id), pushId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this, getString(R.string.channel_id))
                        .setSmallIcon(R.drawable.ic_multy)
                        .setColor(getResources().getColor(R.color.colorPrimaryDark))
                        .setContentTitle("New action in Multy wallet")
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
