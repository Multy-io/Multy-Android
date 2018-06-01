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
import io.multy.R;
import io.multy.api.socket.BlueSocketManager;
import io.multy.api.socket.TransactionUpdateResponse;
import io.multy.model.entities.FastReceiver;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.viewmodels.AssetRequestViewModel;
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
    @BindView(R.id.my_id)
    TextView textMyId;

    private long requestSumSatoshi = 50000;
    private AssetRequestViewModel viewModel;
    private BlueSocketManager socketManager = new BlueSocketManager();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_my_receive_test, container, false);
        ButterKnife.bind(this, convertView);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);
        setBaseViewModel(viewModel);
        return convertView;
    }

    public void setupRequestSum() {
        final String address = viewModel.getWallet().getActiveAddress().getAddress();
        textAddress.setText(address);
        imageFastId.setImageResource(FastReceiver.getImageResId(address));
        textAmount.setText(CryptoFormatUtils.satoshiToBtc(requestSumSatoshi) + " BTC / " + CryptoFormatUtils.satoshiToUsd(requestSumSatoshi) + viewModel.getWallet().getFiatString());
    }

    public void becomeReceiver() {
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        AdvertiseSettings advertiseSettings = buildAdvertiseSettings();
        final String userCode = stringToHex(textAddress.getText().toString().substring(0, 4).getBytes());
        getActivity().runOnUiThread(() -> textMyId.setText(userCode));
        AdvertiseData advertiseData = buildAdvertiseData(userCode);
        advertiser.startAdvertising(advertiseSettings, advertiseData, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userid", RealmManager.getSettingsDao().getUserId().getUserId());
                    jsonObject.put("usercode", userCode);
                    jsonObject.put("currencyid", viewModel.getWallet().getCurrencyId());
                    jsonObject.put("networkid", viewModel.getWallet().getNetworkId());
                    jsonObject.put("address", textAddress.getText().toString());
                    jsonObject.put("amount", String.valueOf(requestSumSatoshi));
                    socketManager.becomeReceiver(jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Toast.makeText(getActivity(), BuildConfig.DEBUG ?
                        "Failed to start broadcasting request. Error code - " + errorCode :
                        getString(R.string.error), Toast.LENGTH_SHORT).show();
                super.onStartFailure(errorCode);
            }
        });
    }

    private String stringToHex(byte[] buf) {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel.getAmount() != 0) {
            updateRequestAmount(viewModel.getAmount());
        }

        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(enableLocationIntent, REQUEST_CODE_LOCATION);
        }


        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_CODE_BLUETOOTH);
        } else {
            start();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_BLUETOOTH && resultCode == Activity.RESULT_OK) {
            start();
        }
    }

    private void start() {
        if (socketManager.getSocket() == null || !socketManager.getSocket().connected()) {
            socketManager.connect();
            socketManager.getSocket().on(EVENT_PAY_RECEIVE, args -> {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            });
            socketManager.getSocket().on(Socket.EVENT_CONNECT, args -> becomeReceiver());
            socketManager.getSocket().on(EVENT_TRANSACTION_UPDATE_BTC, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    getActivity().runOnUiThread(() -> verifyTransactionUpdate(args[0].toString()));
                }
            });
            socketManager.getSocket().on(EVENT_TRANSACTION_UPDATE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    verifyTransactionUpdate(args[0].toString());
                }
            });
        }
    }

    private void verifyTransactionUpdate(String json) {
        TransactionUpdateResponse response = new Gson().fromJson(json, TransactionUpdateResponse.class);
        if (response != null) {
            final String address = viewModel.getWallet().getActiveAddress().getAddress();
            if (response.getEntity().getAddress().equals(address) && response.getEntity().getType() == Constants.TX_MEMPOOL_INCOMING) {
                if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
                    socketManager.disconnect();
                }
                CompleteDialogFragment.newInstance(viewModel.getWallet().getCurrencyId()).show(getActivity().getSupportFragmentManager(), "");
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    private void updateRequestAmount(double btcAmount) {
        requestSumSatoshi = CryptoFormatUtils.btcToSatoshi(String.valueOf(btcAmount));
        setupRequestSum();
    }

    private AdvertiseData buildAdvertiseData(String code) {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString("00000000-0000-0000-0000-000000" + code));
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

    @OnClick(R.id.button_cancel)
    public void onClickCancel() {
        disconnect();
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }
}
