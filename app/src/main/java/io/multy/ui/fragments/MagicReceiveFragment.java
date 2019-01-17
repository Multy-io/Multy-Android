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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import io.multy.api.socket.SocketManager;
import io.multy.api.socket.TransactionUpdateResponse;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.socket.ReceiveMessage;
import io.multy.service.BluetoothService;
import io.multy.storage.RealmManager;
import io.multy.storage.SettingsDao;
import io.multy.ui.Hash2PicView;
import io.multy.ui.activities.MainActivity;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetRequestViewModel;
import io.realm.Realm;
import io.socket.client.Socket;

import static io.multy.api.socket.BlueSocketManager.EVENT_PAY_RECEIVE;
import static io.multy.api.socket.BlueSocketManager.EVENT_TRANSACTION_UPDATE;
import static io.multy.util.Constants.EXTRA_USER_CODE;
import static io.multy.util.Constants.START_BROADCAST;

public class MagicReceiveFragment extends BaseFragment {

    public static final int REQUEST_CODE_LOCATION = 523;
    public static final int REQUEST_CODE_BLUETOOTH = 524;

    @BindView(R.id.image_fast_icon)
    Hash2PicView imageFastId;
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

    private boolean mBound = false;
    BluetoothService mBluetoothService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            BluetoothService.BluetoothBinder binder = (BluetoothService.BluetoothBinder) service;
            mBluetoothService = binder.getService();
            mBluetoothService.listenModeChanging(viewModel.getModeChangingLiveData());

            start();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_magic_receive, container, false);
        ButterKnife.bind(this, convertView);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);
        setBaseViewModel(viewModel);
        viewModel.getModeChangingLiveData().observe(this, mode -> {
            Log.d("MAGIC_TEST", "MODE CHANGED " + mode);
            switch (mode) {
                case BROADCASTER:
                    connectSockets();
                    break;
            }
        });

        return convertView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), BluetoothService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
        stop();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BLUETOOTH && resultCode != Activity.RESULT_OK && getActivity() != null) {
            close();
        }
    }

    private void start() {
        if (mBluetoothService != null) {
            mBluetoothService.startAdvertise(viewModel.getUserCode());
        }
    }

    private void stop() {
        if (mBluetoothService != null) {
            disconnectSockets();
        }
    }

    private void connectSockets() {
        if (socketManager.getSocket() == null || !socketManager.getSocket().connected()) {
            socketManager.connect();
            socketManager.getSocket().on(EVENT_PAY_RECEIVE, args -> {
                getActivity().setResult(Activity.RESULT_OK);
                close();
            });

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userid", viewModel.getUserId());
                jsonObject.put("usercode", viewModel.getUserCode());
                jsonObject.put("currencyid", viewModel.getChainId());
                jsonObject.put("networkid", viewModel.getNetworkId());
                jsonObject.put("address", viewModel.getAddress().getValue());
                jsonObject.put("amount", viewModel.getAmountInMinimalUnits());
                socketManager.getSocket().on(Socket.EVENT_CONNECT, args -> {
                    Log.d("MAGIC_TEST", "SOCKETS CONNECTED");
                    socketManager.becomeReceiver(jsonObject);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            socketManager.getSocket().on(Socket.EVENT_DISCONNECT, args -> {
                Log.d("MAGIC_TEST", "SOCKETS DISCONNECTED");
                mBluetoothService.stopAdvertise();
            });
            socketManager.getSocket().on(EVENT_TRANSACTION_UPDATE, args -> getActivity().runOnUiThread(() -> verifyTransactionUpdate(args[0].toString())));
            socketManager.getSocket().on(SocketManager.getEventReceive(RealmManager.getSettingsDao().getUserId().getUserId()), args -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            verifyTransaction(new Gson().fromJson(args[0].toString(), ReceiveMessage.class));
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            });
        }
    }

    public void close() {
        if (getActivity().getIntent().hasExtra(Constants.EXTRA_DEEP_MAGIC)) {
            Intent intent = new Intent(Multy.getContext(), MainActivity.class);
            intent.removeExtra(Constants.EXTRA_DEEP_MAGIC);
            startActivity(intent);
            getActivity().finish();
        } else {
            getActivity().finish();
        }
    }

    private void disconnectSockets() {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            socketManager.disconnect();
        }
    }

    public void setupRequestSum() {
        final String address = viewModel.getWallet().getActiveAddress().getAddress();
        textAddress.setText(address);
        imageFastId.setAvatar(address);
//        imageFastId.setImageResource(FastReceiver.getImageResId(address));
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

    private void verifyTransaction(ReceiveMessage receiveMessage) {
        final String address = viewModel.getWallet().getActiveAddress().getAddress();
        final ReceiveMessage.Payload payload = receiveMessage.getPayload();
        if (payload.getTo().equals(address) && (receiveMessage.getType() == Constants.EVENT_TYPE_NOTIFY_PAYMENT_REQUEST ||
                receiveMessage.getType() == Constants.EVENT_TYPE_NOTIFY_INCOMING_TX)) {
            if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
                socketManager.disconnect();
            }
            String amount = "";
            switch (NativeDataHelper.Blockchain.valueOf(viewModel.getWallet().getCurrencyId())) {
                case BTC:
                    amount = CryptoFormatUtils.satoshiToBtcLabel(Long.parseLong(receiveMessage.getPayload().getAmount()));
                    break;
                case ETH:
                    amount = CryptoFormatUtils.weiToEthLabel(receiveMessage.getPayload().getAmount());
                    break;
            }
            Analytics.getInstance(getActivity()).logEvent(AnalyticsConstants.KF_RECEIVED_TRANSACTION, AnalyticsConstants.KF_RECEIVED_TRANSACTION, "true");
            CompleteDialogFragment.newInstance(viewModel.getWallet().getCurrencyId(), amount,
                    receiveMessage.getPayload().getFrom()).show(getActivity().getSupportFragmentManager(), "");
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
                        response.getEntity().getAddressFrom()).show(getActivity().getSupportFragmentManager(), "");
            }
        }
    }

    @OnClick(R.id.button_cancel)
    public void onClickCancel() {
        stop();
        getActivity().setResult(Activity.RESULT_CANCELED);
        close();
    }
}
