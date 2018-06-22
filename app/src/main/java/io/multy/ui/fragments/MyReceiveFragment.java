/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.BuildConfig;
import io.multy.Multy;
import io.multy.R;
import io.multy.api.socket.BlueSocketManager;
import io.multy.api.socket.TransactionUpdateResponse;
import io.multy.model.entities.FastReceiver;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.storage.SettingsDao;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetRequestViewModel;
import io.realm.Realm;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static io.multy.api.socket.BlueSocketManager.EVENT_PAY_RECEIVE;
import static io.multy.api.socket.BlueSocketManager.EVENT_TRANSACTION_UPDATE;
import static io.multy.api.socket.BlueSocketManager.EVENT_TRANSACTION_UPDATE_BTC;

public class MyReceiveFragment extends BaseFragment {

    public static final int REQUEST_CODE_LOCATION = 523;
    public static final int REQUEST_CODE_BLUETOOTH = 524;
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    @BindView(R.id.image_fast_icon)
    ImageView imageFastId;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.text_amount)
    TextView textAmount;
    @BindView(R.id.animation_view)
    LottieAnimationView lottieAnimationView;
    @BindView(R.id.root)
    View parent;

    private AssetRequestViewModel viewModel;
    private BlueSocketManager socketManager = new BlueSocketManager();
    private Callback callback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callback = new Callback();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_my_receive_test, container, false);
        ButterKnife.bind(this, convertView);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);
        setBaseViewModel(viewModel);
        return convertView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel.getAmount() != 0) {
            setupRequestSum();
        }

        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(enableLocationIntent, REQUEST_CODE_LOCATION);
        }

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_CODE_BLUETOOTH);
        } else {
            start();
        }
    }

    @Override
    public void onPause() {
        if (callback != null && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser().stopAdvertising(callback);
            disconnect();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        callback = null;
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BLUETOOTH && resultCode != Activity.RESULT_OK && getActivity() != null) {
            getActivity().finish();
        }
    }

    private void start() {
        if (socketManager.getSocket() == null || !socketManager.getSocket().connected()) {
            socketManager.connect();
            socketManager.getSocket().on(EVENT_PAY_RECEIVE, args -> {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            });
            final double amount = viewModel.getAmount();
            final int currencyId = viewModel.getWallet().getCurrencyId();
            socketManager.getSocket().on(Socket.EVENT_CONNECT, args -> becomeReceiver(amount, currencyId));
            socketManager.getSocket().on(EVENT_TRANSACTION_UPDATE_BTC, args -> getActivity().runOnUiThread(() -> verifyTransactionUpdate(args[0].toString())));
            socketManager.getSocket().on(EVENT_TRANSACTION_UPDATE, args -> getActivity().runOnUiThread(() -> verifyTransactionUpdate(args[0].toString())));
        }
    }

    private String stringToHex(byte[] buf) {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }

    private void verifyTransactionUpdate(String json) {
        TransactionUpdateResponse response = new Gson().fromJson(json, TransactionUpdateResponse.class);
        if (response != null) {
            final String address = viewModel.getWallet().getActiveAddress().getAddress();
            if (response.getEntity().getAddress().equals(address) && response.getEntity().getType() == Constants.TX_MEMPOOL_INCOMING) {
                if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
                    socketManager.disconnect();
                }
                String amount = "";
                switch (NativeDataHelper.Blockchain.valueOf(viewModel.getWallet().getCurrencyId())) {
                    case BTC:
                        amount = CryptoFormatUtils.satoshiToBtcLabel((long) response.getEntity().getAmount());
                        break;
                    case ETH:
                        amount = CryptoFormatUtils.weiToEthLabel(CryptoFormatUtils.FORMAT_ETH.format(response.getEntity().getAmount()));
                        break;
                }
                Analytics.getInstance(getActivity()).logEvent(AnalyticsConstants.KF_RECEIVED_TRANSACTION, AnalyticsConstants.KF_RECEIVED_TRANSACTION, "true");
                CompleteDialogFragment.newInstance(viewModel.getWallet().getCurrencyId(), amount,
                        response.getEntity().getAddress()).show(getActivity().getSupportFragmentManager(), "");
            }
        }
    }

    private AdvertiseData buildAdvertiseData(String code) {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString("8c0d3334-7711-44e3-b5c4-28b2" + code));
        dataBuilder.setIncludeTxPowerLevel(true);
        dataBuilder.addServiceUuid(pUuid);
        return dataBuilder.build();
    }


    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        settingsBuilder.setConnectable(false);
        return settingsBuilder.build();
    }

    private void disconnect() {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            socketManager.disconnect();
        }
    }

    public void setupRequestSum() {
        final String address = viewModel.getWallet().getActiveAddress().getAddress();
        textAddress.setText(address);
        imageFastId.setImageResource(FastReceiver.getImageResId(address));
        String amount = "";
        String fiatAmount = "";
        switch (NativeDataHelper.Blockchain.valueOf(viewModel.getWallet().getCurrencyId())) {
            case BTC:
                amount = CryptoFormatUtils.FORMAT_BTC.format(viewModel.getAmount());
                fiatAmount = CryptoFormatUtils.btcToUsd(viewModel.getAmount());
                break;
            case ETH:
                amount = CryptoFormatUtils.FORMAT_ETH.format(viewModel.getAmount());
                fiatAmount = CryptoFormatUtils.ethToUsd(viewModel.getAmount());
                break;
        }
        textAmount.setText(String.format("%s %s / %s %s", amount, viewModel.getWallet().getCurrencyName(),
                fiatAmount, CurrencyCode.USD.name()));
    }

    public void becomeReceiver(double amount, int currencyId) {
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        AdvertiseSettings advertiseSettings = buildAdvertiseSettings();
        final String userCode = stringToHex(textAddress.getText().toString().substring(0, 4).getBytes());
        AdvertiseData advertiseData = buildAdvertiseData(userCode);
        callback.setUserCode(userCode);
        String requestSum = "0";
        switch (NativeDataHelper.Blockchain.valueOf(currencyId)) {
            case BTC:
                requestSum = CryptoFormatUtils.btcToSatoshiString(amount);
                break;
            case ETH:
                requestSum = CryptoFormatUtils.ethToWei(String.valueOf(amount));
                break;
        }
        callback.setRequestSum(requestSum);
        advertiser.startAdvertising(advertiseSettings, advertiseData, callback);
    }

    @OnClick(R.id.button_cancel)
    public void onClickCancel() {
        disconnect();
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    private class Callback extends AdvertiseCallback {

        private String userCode = "";
        private String requestSum = "0";

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Realm realm = Realm.getInstance(Multy.getRealmConfiguration());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userid", new SettingsDao(realm).getUserId().getUserId());
                jsonObject.put("usercode", userCode);
                jsonObject.put("currencyid", viewModel.getWallet().getCurrencyId());
                jsonObject.put("networkid", viewModel.getWallet().getNetworkId());
                jsonObject.put("address", textAddress.getText().toString());
                jsonObject.put("amount", String.valueOf(requestSum));

                socketManager.becomeReceiver(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            realm.close();
        }

        @Override
        public void onStartFailure(int errorCode) {
            Toast.makeText(getActivity(), BuildConfig.DEBUG ?
                    "Failed to start broadcasting request. Error code - " + errorCode :
                    getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }

        public String getUserCode() {
            return userCode;
        }

        public void setUserCode(String userCode) {
            this.userCode = userCode;
        }

        public String getRequestSum() {
            return requestSum;
        }

        public void setRequestSum(String requestSum) {
            this.requestSum = requestSum;
        }
    }
}
