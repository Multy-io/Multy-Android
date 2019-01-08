package io.multy.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.v4.app.NotificationCompat;

import java.util.UUID;

import io.multy.R;
import io.multy.ui.activities.SplashActivity;

import static io.multy.util.Constants.BLE_SERVICE_UUID_PREFIX;
import static io.multy.util.Constants.BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_ID;
import static io.multy.util.Constants.BLUETOOTH_SERVICE_NOTIFICATION_ID;
import static io.multy.util.Constants.EXTRA_USER_CODE;
import static io.multy.util.Constants.START_BROADCAST;
import static io.multy.util.Constants.START_SCAN;
import static io.multy.util.Constants.START_SCAN_AND_BROADCAST;
import static io.multy.util.Constants.START_SERVICE;
import static io.multy.util.Constants.STOP_ACTION;
import static io.multy.util.Constants.STOP_SERVICE;

public class BluetoothService extends Service {

    public enum BluetoothServiceMode { STOPPED, STARTED, SCANNER, BROADCASTER, HYBRID /* scanner + broadcaster */}

    public boolean isBluetoothTransportReachable() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    private MutableLiveData<Boolean> reachabilityLiveData = new MutableLiveData<>();
    private MutableLiveData<BluetoothServiceMode> modeChangingLiveData = new MutableLiveData<>();

    public BluetoothServiceMode serviceMode = BluetoothServiceMode.STOPPED;

    private UserCodeAdvertiseCallback userCodeAdvertiseCallback;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                onReachabilityChanged();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();

        final String userCode = intent.getStringExtra(EXTRA_USER_CODE);

        switch (action) {
            case START_SERVICE:
                startForegroundService();
                break;

            case START_BROADCAST:
                startAdvertise(userCode);
                break;

            case START_SCAN:

                break;

            case START_SCAN_AND_BROADCAST:

                break;

            case STOP_ACTION:
                stopAction();
                break;

            case STOP_SERVICE:
                stopForegroundService();
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void listenReachability(MutableLiveData<Boolean> reachabilityLiveData) {
        this.reachabilityLiveData = reachabilityLiveData;
    }

    public void listenModeChangingLiveData(MutableLiveData<BluetoothServiceMode> modeChangingLiveData) {
        this.modeChangingLiveData = modeChangingLiveData;
    }

    private void onReachabilityChanged() {
        if (reachabilityLiveData != null) {
            reachabilityLiveData.postValue(isBluetoothTransportReachable());
        }
    }

    private void startForegroundService(){
        createNotificationChannel();
        Intent notificationIntent = new Intent();
        PendingIntent pendingIntent=PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification=new NotificationCompat.Builder(this, BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Bluetooth Service")
                .setContentText("Bluetooth Service Description")
                .setContentIntent(pendingIntent).build();

        startForeground(BLUETOOTH_SERVICE_NOTIFICATION_ID, notification);
        serviceMode = BluetoothServiceMode.STARTED;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Bluetooth Channel Title";
            String description = "Bluetooth Channel Text";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startAdvertise(String userCode) {
        if (isBluetoothTransportReachable()) {
            userCodeAdvertiseCallback = new UserCodeAdvertiseCallback();
            userCodeAdvertiseCallback.setUserCode(userCode);

            BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
            AdvertiseSettings advertiseSettings = buildAdvertiseSettings();
            AdvertiseData advertiseData = buildAdvertiseData(userCode);

            advertiser.startAdvertising(advertiseSettings, advertiseData, userCodeAdvertiseCallback);
        }
    }

    private void stopAction() {
        switch (serviceMode) {
            case STARTED:
                break;

            case STOPPED:
                return;

            case SCANNER:
                break;

            case BROADCASTER:
                stopAdvertise();
                break;

            case HYBRID:
                break;
        }

        serviceMode = BluetoothServiceMode.STARTED;
        modeChangingLiveData.postValue(serviceMode);
    }

    private void stopAdvertise() {
        if (serviceMode == BluetoothServiceMode.BROADCASTER) {
            BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
            advertiser.stopAdvertising(userCodeAdvertiseCallback);
            userCodeAdvertiseCallback = null;
        }
    }

    private void stopForegroundService()
    {
        stopForeground(true);

        stopSelf();
    }

    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        settingsBuilder.setConnectable(false);
        return settingsBuilder.build();
    }

    private AdvertiseData buildAdvertiseData(String code) {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(BLE_SERVICE_UUID_PREFIX + code));
        dataBuilder.setIncludeTxPowerLevel(true);
        dataBuilder.addServiceUuid(pUuid);
        return dataBuilder.build();
    }

    private class UserCodeAdvertiseCallback extends AdvertiseCallback {

        private String userCode;

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            serviceMode = BluetoothServiceMode.BROADCASTER;
            modeChangingLiveData.postValue(serviceMode);
        }

        @Override
        public void onStartFailure(int errorCode) {}

        public void setUserCode(String userCode) {
            this.userCode = userCode;
        }

        public String getUserCode() {
            return userCode;
        }
    }
}
