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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.multy.R;

import static io.multy.util.Constants.MULTY_UUID_PREFIX;
import static io.multy.util.Constants.BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_ID;
import static io.multy.util.Constants.BLUETOOTH_SERVICE_NOTIFICATION_ID;
import static io.multy.util.Constants.START_SERVICE;


public class BluetoothService extends Service {

    public enum BluetoothServiceMode { STOPPED, STARTED, SCANNER, BROADCASTER, HYBRID /* scanner + broadcaster */}
    public boolean isBluetoothTransportReachable() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }
    public BluetoothServiceMode serviceMode = BluetoothServiceMode.STOPPED;

    private IBinder mBinder = new BluetoothBinder();
    private MutableLiveData<BluetoothServiceMode> modeChangingLiveData;
    private MutableLiveData<ArrayList<String>> userCodesLiveData;
    private ArrayList<String> userCodes = new ArrayList<>();
    private UserCodeAdvertiseCallback userCodeAdvertiseCallback;
    private UserCodeScanCallback userCodeScanCallback;

    @Override
    public void onCreate() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // UNCOMMENT IF FOREGROUND SERVICE NEEDED
//        final String action = intent.getAction();
//        if (action != null) {
//            switch (action) {
//                case START_SERVICE:
//                    startForegroundService();
//                    break;
//
//            }
//        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.d("MAGIC_TEST", "SERVICE DESTROYED");
        super.onDestroy();
    }

    // UNCOMMENT IF FOREGROUND SERVICE NEEDED
//    private void startForegroundService(){
//        createNotificationChannel();
//        Intent notificationIntent = new Intent();
//        PendingIntent pendingIntent=PendingIntent.getActivity(this, 0,
//                notificationIntent, 0);
//
//        Notification notification=new NotificationCompat.Builder(this, BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_ID)
//                .setSmallIcon(R.drawable.logo)
//                .setContentTitle("Bluetooth Service")
//                .setContentText("Bluetooth Service Description")
//                .setContentIntent(pendingIntent).build();
//
//        startForeground(BLUETOOTH_SERVICE_NOTIFICATION_ID, notification);
//        setServiceMode(BluetoothServiceMode.STARTED);
//    }

//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Bluetooth Channel Title";
//            String description = "Bluetooth Channel Text";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }

    public void startAdvertise(String userCode) {
        if (isBluetoothTransportReachable()) {
            userCodeAdvertiseCallback = new UserCodeAdvertiseCallback();
            userCodeAdvertiseCallback.setUserCode(userCode);

            BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
            AdvertiseSettings advertiseSettings = buildAdvertiseSettings();
            AdvertiseData advertiseData = buildAdvertiseData(userCode);

            advertiser.startAdvertising(advertiseSettings, advertiseData, userCodeAdvertiseCallback);
        }
    }

    public void stopAdvertise() {
        if (serviceMode == BluetoothServiceMode.BROADCASTER) {
            BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
            advertiser.stopAdvertising(userCodeAdvertiseCallback);
            userCodeAdvertiseCallback = null;
            setServiceMode(BluetoothServiceMode.STARTED);
        }
    }

    public void startScan() {
        if (isBluetoothTransportReachable()) {
            userCodeScanCallback = new UserCodeScanCallback();
            ScanSettings settings = buildScanSettings();
            List<ScanFilter> filters = new ArrayList<>();

            BluetoothLeScanner scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            scanner.startScan(filters, settings, userCodeScanCallback);
            setServiceMode(BluetoothServiceMode.SCANNER);
        }
    }

    public void stopScan() {
        if (serviceMode == BluetoothServiceMode.SCANNER) {
            BluetoothLeScanner scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            scanner.stopScan(userCodeScanCallback);
            userCodeScanCallback = null;
            userCodes.clear();
            setServiceMode(BluetoothServiceMode.STARTED);
        }
    }

    private void stopForegroundService()
    {
        // UNCOMMENT IF FOREGROUND SERVICE NEEDED
 //       stopForeground(true);
        stopSelf();
    }

    public void listenModeChanging(MutableLiveData<BluetoothServiceMode> modeChangingLiveData) {
        this.modeChangingLiveData = modeChangingLiveData;
    }

    public void listenUserCodes(MutableLiveData<ArrayList<String>> userCodesLiveData) {
        this.userCodesLiveData = userCodesLiveData;
    }

    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        settingsBuilder.setConnectable(false);
        return settingsBuilder.build();
    }

    private AdvertiseData buildAdvertiseData(String code) {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(MULTY_UUID_PREFIX + code));
        dataBuilder.setIncludeTxPowerLevel(true);
        dataBuilder.addServiceUuid(pUuid);
        return dataBuilder.build();
    }

    private ScanSettings buildScanSettings() {
        ScanSettings result = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
        return result;
    }

    public class BluetoothBinder extends Binder {
        public  BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private void setServiceMode(BluetoothServiceMode serviceMode) {
        if (this.serviceMode != serviceMode) {
            this.serviceMode = serviceMode;

            if (modeChangingLiveData != null) {
                modeChangingLiveData.postValue(serviceMode);
            }
        }
    }

    private class UserCodeAdvertiseCallback extends AdvertiseCallback {

        private String userCode;

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            setServiceMode(BluetoothServiceMode.BROADCASTER);
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

    private class UserCodeScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            List<ParcelUuid> ids = result.getScanRecord().getServiceUuids();

            if (ids != null && ids.size() > 0) {
                for (ParcelUuid uuid : ids) {
                    if (isServiceUuidValid(uuid)) {
                        String sendUUID = uuid.getUuid().toString();

                        String userCode = sendUUID.substring(sendUUID.length() - 8).toUpperCase();

                        if (userCode.length() > 0 && !userCodes.contains(userCode)) {
                            userCodes.add(userCode);
                            userCodesLiveData.postValue(userCodes);
                        }
                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            if (serviceMode == BluetoothServiceMode.SCANNER) {
                setServiceMode(BluetoothServiceMode.STARTED);
            }
        }


        private boolean isServiceUuidValid(ParcelUuid uuid) {
            boolean result = false;
            String uuidString = uuid.toString();
            String uuidPrefix = uuidString.substring(0, MULTY_UUID_PREFIX.length());
            if (uuidPrefix.equals(MULTY_UUID_PREFIX)) {
                result = true;
            }

            return result;
        }
    }
}
