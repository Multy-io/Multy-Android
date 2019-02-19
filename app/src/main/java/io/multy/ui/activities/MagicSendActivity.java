/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.BlueSocketManager;
import io.multy.api.socket.SocketManager;
import io.multy.model.entities.Estimation;
import io.multy.model.entities.FastReceiver;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletPrivateKey;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.model.responses.FeeRateResponse;
import io.multy.model.responses.MessageResponse;
import io.multy.model.responses.WalletsResponse;
import io.multy.service.BluetoothService;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.MyWalletPagerAdapter;
import io.multy.ui.adapters.ReceiversPagerAdapter;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.MagicSendViewModel;
import io.realm.RealmResults;
import io.socket.client.Ack;
import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static io.multy.api.socket.BlueSocketManager.EVENT_SENDER_CHECK;
import static io.multy.ui.fragments.send.SendSummaryFragment.byteArrayToHex;

public class MagicSendActivity extends BaseActivity {
    private static final String TAG = MagicSendActivity.class.getSimpleName();
    private static final String TAG_SEND = "Send";
    public static final int UPDATE_PERIOD = 5000;
    public static final int REQUEST_BLUETOOTH = 1;
    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 2;
    public static final int REQUEST_CODE_LOCATION = 3;

    @BindView(R.id.pager_wallets)
    ViewPager pagerWallets;
    @BindView(R.id.pager_requests)
    ViewPager pagerRequests;
    @BindView(R.id.container_send)
    View containerSend;

    @BindView(R.id.text_send_amount)
    TextView textSendAmount;
    @BindView(R.id.text_send_amount_fiat)
    TextView textSendAmountFiat;
    @BindView(R.id.image_coin)
    ImageView imageCoin;
    @BindView(R.id.text_scanning)
    TextView textScanning;
    @BindView(R.id.text_hint)
    TextView textHint;

    private ReceiversPagerAdapter receiversPagerAdapter;
    private MyWalletPagerAdapter walletPagerAdapter;
//    private BlueSocketManager socketManager;
    private SocketManager socketManager;
    private ArrayList<String> leIds = new ArrayList<>();
    private long selectedWalletId = -1;
    private Handler handler = new Handler();
    private Runnable updateReceiversAction;
    private Handler animationHandler = new Handler();
    private float startY;
    private boolean mBound = false;
    BluetoothService mBluetoothService;
    private MagicSendViewModel viewModel;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            BluetoothService.BluetoothBinder binder = (BluetoothService.BluetoothBinder) service;
            mBluetoothService = binder.getService();
            mBluetoothService.listenModeChanging(viewModel.getModeChangingLiveData());
            mBluetoothService.listenUserCodes(viewModel.getUserCodesLiveData());

            startBleScanner();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magic_send);
        ButterKnife.bind(this);
        Analytics.getInstance(this).logActivityLaunch(MagicSendActivity.class.getSimpleName());
        viewModel = ViewModelProviders.of(this).get(MagicSendViewModel.class);
        initUpdateAction();
        initPagers();
        initContainerSend();

        if (walletPagerAdapter != null) {
            Analytics.getInstance(this).logEvent(AnalyticsConstants.KF_WALLET_COUNT, AnalyticsConstants.KF_WALLET_COUNT, String.valueOf(walletPagerAdapter.getCount()));
        }

        // TODO: need to refactor this
        viewModel.getModeChangingLiveData().observe(this, mode -> {
            Log.d("MAGIC_TEST", "MODE CHANGED " + mode);
        });

        viewModel.getUserCodesLiveData().observe(this, userCodes -> {
            this.leIds = userCodes;
        });

        containerSend.post(() -> startY = containerSend.getY());
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    @Override
    protected void onPause() {
        stop();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
//        stop();

        if (receiversPagerAdapter != null) {
            Analytics.getInstance(this).logEvent(AnalyticsConstants.KF_FOUND_DEVICES, AnalyticsConstants.KF_FOUND_DEVICES, String.valueOf(receiversPagerAdapter.getCount()));
        }
        Analytics.getInstance(this).logActivityClose(MagicSendActivity.class.getSimpleName());
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            final String address = data.getStringExtra(Constants.EXTRA_QR_CONTENTS);
            Intent intent = new Intent(this, AssetSendActivity.class);
            intent.putExtra(Constants.EXTRA_ADDRESS, address);
            startActivity(intent);
            finish();
        } else if (requestCode == REQUEST_BLUETOOTH && resultCode == RESULT_OK) {
            Analytics.getInstance(this).logEvent(AnalyticsConstants.KF_PERMISSIONS_GRANTED, AnalyticsConstants.KF_PERMISSIONS_GRANTED, "BT_on");
            grantBluetooth();
        } else if (requestCode != REQUEST_CODE_LOCATION) {
            Analytics.getInstance(this).logEvent(AnalyticsConstants.KF_PERMISSIONS_GRANTED, AnalyticsConstants.KF_PERMISSIONS_GRANTED, "BT_off");
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);

                for (int i = 0; i < permissions.length; i++) {
                    perms.put(permissions[i], grantResults[i]);
                }

                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Analytics.getInstance(this).logEvent(AnalyticsConstants.KF_PERMISSIONS_GRANTED, Manifest.permission.ACCESS_FINE_LOCATION, String.valueOf(true));
                    grantBluetooth();
                } else {
                    Analytics.getInstance(this).logEvent(AnalyticsConstants.KF_PERMISSIONS_GRANTED, Manifest.permission.ACCESS_FINE_LOCATION, String.valueOf(false));
                    finish();
                }
                break;
            case Constants.CAMERA_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(this, ScanActivity.class), Constants.CAMERA_REQUEST_CODE);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        if (containerSend.getVisibility() == View.VISIBLE) {
            hideSendState();
            enableScroll();
            receiversPagerAdapter.showElements(pagerRequests.getCurrentItem());
            walletPagerAdapter.showElements(pagerWallets.getCurrentItem());
        } else {
            super.onBackPressed();
        }
    }

    private void initPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        } else {
            grantBluetooth();
        }
    }

    private void startSockets() {
        socketManager = SocketManager.getInstance();
        socketManager.connect(TAG);
        startBleScanner();
        if (socketManager.isConnected()){
            handler.postDelayed(updateReceiversAction, UPDATE_PERIOD);
            startBleScanner();
        }
//        if (socketManager == null || !socketManager.getSocket().connected()) {
//            socketManager = new BlueSocketManager();
//            socketManager.connect();
//            socketManager.getSocket().on(Socket.EVENT_CONNECT, args -> {
//                handler.postDelayed(updateReceiversAction, UPDATE_PERIOD);
//                startBleScanner();
//            });
//        }
    }

    private void initContainerSend() {
        containerSend.setTag(TAG_SEND);
    }

    private void initPagers() {
        initPagerWallets();
        receiversPagerAdapter = new ReceiversPagerAdapter(getSupportFragmentManager());
        pagerRequests.setAdapter(receiversPagerAdapter);
        pagerRequests.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                initPagerWallets();
            }
        });
    }

    private void initPagerWallets() {
        FastReceiver receiver = null;
        try {
            receiver = receiversPagerAdapter.getReceiver(pagerRequests.getCurrentItem());
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Wallet> wallets;

        if (receiver == null) {
            wallets = RealmManager.getAssetsDao().getAvailableWallets();
        } else {
            wallets = new ArrayList<>();
            RealmResults<Wallet> resultWallets = RealmManager.getAssetsDao()
                    .getAvailableWallets(receiver.getCurrencyId(), receiver.getNetworkId());
            final BigInteger receiverAmount = new BigInteger(receiver.getAmount());
            for (Wallet wallet : resultWallets) {
                BigInteger availableBalance = new BigInteger(wallet.getAvailableBalance());
                if (availableBalance.compareTo(receiverAmount) >= 0) {
                    wallets.add(wallet);
                }
            }
        }

        walletPagerAdapter = new MyWalletPagerAdapter(getSupportFragmentManager(), new View.OnTouchListener() {
            private float rawY = 0.0f;
            private float sendStartY = 0.0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (containerSend.getVisibility() != View.VISIBLE && receiversPagerAdapter != null && receiversPagerAdapter.getCount() > 0) {
                            animationHandler.postDelayed(() -> showSendState(), ViewConfiguration.getLongPressTimeout());
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (containerSend.getVisibility() == View.VISIBLE) {
                            if (rawY == 0.0f) {
                                rawY = event.getY();
                            }

                            if (sendStartY == 0.0f) {
                                sendStartY = containerSend.getY();
                            }

                            final float endY = sendStartY + (event.getY() - rawY);

                            if (endY < sendStartY) {
                                containerSend.setY(endY);
                            }

                            if (event.getRawY() < pagerRequests.getBottom() && event.getRawY() > pagerRequests.getTop()) {
                                receiversPagerAdapter.setGreenState(pagerRequests.getCurrentItem());
                            } else {
                                receiversPagerAdapter.resetColorState(pagerRequests.getCurrentItem());
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        animationHandler.removeCallbacksAndMessages(null);

                        if (containerSend.getVisibility() == View.VISIBLE) {
                            receiversPagerAdapter.resetColorState(pagerRequests.getCurrentItem());
                            if (event.getRawY() < pagerRequests.getBottom() && event.getRawY() > pagerRequests.getTop()) {
                                animateSend();
                            } else {
                                hideSendState();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        animationHandler.removeCallbacksAndMessages(null);
                        hideSendState();
                        break;
                }
                return true;
            }
        }, wallets);
        pagerWallets.setAdapter(walletPagerAdapter);
        pagerWallets.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                animationHandler.removeCallbacksAndMessages(null);
                if (containerSend.getVisibility() == View.VISIBLE) {
                    hideSendState();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (walletPagerAdapter != null && receiversPagerAdapter != null && walletPagerAdapter.getCount() > 0 && receiversPagerAdapter.getCount() > 0) {
            textHint.setVisibility(View.VISIBLE);
        } else {
            textHint.setVisibility(View.GONE);
        }
    }

    private void updateLocalWallets() {
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletsResponse> call, @NonNull Response<WalletsResponse> response) {
                WalletsResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getWallets() != null) {
                    RealmManager.getAssetsDao().saveWallets(body.getWallets());
                    initPagerWallets();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WalletsResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

//    v -> {
//        handler.removeCallbacksAndMessages(null);
//        if (containerSend.getVisibility() == View.VISIBLE) {
//            hideSendGroup();
//            enableScroll();
//            showPagerElements();
//            handler.postDelayed(updateReceiversAction, UPDATE_PERIOD / 5);
//        } else if (receiversPagerAdapter.getCount() > 0) {
//            showSendState();
//            disableScroll();
//            selectedWalletId = (long) v.getTag();
//            hidePagerElements();
//        }
//    }

    private void showPagerElements() {
        receiversPagerAdapter.showElements(pagerRequests.getCurrentItem());
        walletPagerAdapter.showElements(pagerWallets.getCurrentItem());
        selectedWalletId = -1;
    }

    private void hidePagerElements() {
        receiversPagerAdapter.hideElements(pagerRequests.getCurrentItem());
        walletPagerAdapter.hideElements(pagerWallets.getCurrentItem());
    }

    private void enableScroll() {
        pagerWallets.setOnTouchListener(null);
        pagerRequests.setOnTouchListener(null);
    }

    private void disableScroll() {
        pagerWallets.setOnTouchListener((v, event) -> true);
        pagerRequests.setOnTouchListener((v, event) -> true);
    }

    private void setupSendGroup() {
        String amount = "";
        String amountFiat = "";
        int coinResId = R.drawable.ic_coin_btc;

        FastReceiver receiver = receiversPagerAdapter.getReceiver(pagerRequests.getCurrentItem());
        switch (NativeDataHelper.Blockchain.valueOf(receiver.getCurrencyId())) {
            case BTC:
                final long amountBtc = (long) Double.parseDouble(receiver.getAmount());
                amount = CryptoFormatUtils.satoshiToBtcLabel(amountBtc);
                amountFiat = CryptoFormatUtils.satoshiToUsd(amountBtc) + CurrencyCode.USD.name();
                coinResId = receiver.getNetworkId() == NativeDataHelper.NetworkId.TEST_NET.getValue() ? R.drawable.ic_coint_btc_test : R.drawable.ic_coin_btc;
                break;
            case ETH:
                amount = CryptoFormatUtils.weiToEthLabel(receiver.getAmount());
                amountFiat = CryptoFormatUtils.weiToUsd(new BigInteger(receiver.getAmount())) + " " + CurrencyCode.USD.name();
                if (receiver.getNetworkId() == NativeDataHelper.NetworkId.RINKEBY.getValue()) {
                    coinResId = R.drawable.ic_coin_eth_test;
                } else if (receiver.getNetworkId() == NativeDataHelper.NetworkId.ETH_MAIN_NET.getValue()) {
                    coinResId = R.drawable.ic_eth_medium_icon;
                }
                break;
        }

        imageCoin.setImageResource(coinResId);
        textSendAmount.setText(amount);
        textSendAmountFiat.setText(amountFiat);
    }

    /**
     * Chain of animations. Image coin showing first. containerSend showing after
     */
    private void showSendState() {
        if (receiversPagerAdapter == null || receiversPagerAdapter.getCount() == 0) {
            return;
        }
        handler.removeCallbacksAndMessages(null);
        textHint.setVisibility(View.GONE);
        startY = containerSend.getY();

        final float sendY = containerSend.getHeight() / 2;
        containerSend.animate().translationYBy(sendY).setDuration(0).alpha(0).withEndAction(() -> {
            containerSend.setVisibility(View.VISIBLE);
            containerSend.animate().translationYBy(-sendY).setDuration(100).alpha(1).setInterpolator(new DecelerateInterpolator()).start();
        }).start();

        selectedWalletId = walletPagerAdapter.getSelectedWalletId(pagerWallets.getCurrentItem());
        hidePagerElements();
        setupSendGroup();
    }

    private void hideSendState() {
        final float sendY = containerSend.getHeight() / 2;
        containerSend.animate().translationYBy(sendY).setDuration(100).alpha(0).setInterpolator(new DecelerateInterpolator()).withEndAction(() -> {
            containerSend.setVisibility(View.INVISIBLE);
//            containerSend.setY(startY);
            containerSend.animate().translationY(sendY).setDuration(0).alpha(0).start();
        }).start();

        textHint.setVisibility(View.VISIBLE);
        showPagerElements();
        handler.postDelayed(updateReceiversAction, UPDATE_PERIOD / 5);
    }

    private boolean send(Wallet wallet, String feeRate, @Nullable String estimation) throws JniException {
        final byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();
        FastReceiver receiver = receiversPagerAdapter.getReceiver(pagerRequests.getCurrentItem());
        String changeAddress = "";
        if (wallet.getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()) {
            changeAddress = NativeDataHelper.makeAccountAddress(seed, wallet.getIndex(), wallet.getBtcWallet().getAddresses().size(),
                    wallet.getCurrencyId(), wallet.getNetworkId());
        }

        final byte[] transaction;
        final boolean isHd;
        switch (NativeDataHelper.Blockchain.valueOf(receiver.getCurrencyId())) {
            case BTC:
                transaction = NativeDataHelper.makeTransaction(
                        wallet.getId(),
                        wallet.getNetworkId(),
                        seed,
                        wallet.getIndex(),
                        receiver.getAmount(),
                        feeRate,
                        "0",
                        receiver.getAddress(),
                        changeAddress,
                        "",
                        true);
                isHd = true;
                break;
            case ETH:
                if (wallet.isMultisig()) {
                    Wallet linkedWallet = RealmManager.getAssetsDao().getMultisigLinkedWallet(wallet.getMultisigWallet().getOwners());
                    if (linkedWallet.shouldUseExternalKey()) {
                        WalletPrivateKey keyObject = RealmManager.getAssetsDao().getPrivateKey(linkedWallet.getActiveAddress().getAddress(),
                                linkedWallet.getCurrencyId(), linkedWallet.getNetworkId());
                        transaction = NativeDataHelper.makeTransactionMultisigETHFromKey(
                                keyObject.getPrivateKey(),
                                linkedWallet.getCurrencyId(),
                                linkedWallet.getNetworkId(),
                                linkedWallet.getActiveAddress().getAmountString(),
                                wallet.getActiveAddress().getAddress(),
                                receiver.getAmount(),
                                receiver.getAddress(),
                                estimation, feeRate, linkedWallet.getEthWallet().getNonce());
                    } else {
                        transaction = NativeDataHelper.makeTransactionMultisigETH(
                                seed,
                                linkedWallet.getIndex(),
                                0,
                                linkedWallet.getCurrencyId(),
                                linkedWallet.getNetworkId(),
                                linkedWallet.getActiveAddress().getAmountString(),
                                wallet.getActiveAddress().getAddress(),
                                receiver.getAmount(),
                                receiver.getAddress(),
                                estimation,
                                feeRate,
                                linkedWallet.getEthWallet().getNonce());
                    }
                } else {
                    if (wallet.shouldUseExternalKey()) {
                        WalletPrivateKey keyObject = RealmManager.getAssetsDao().getPrivateKey(wallet.getActiveAddress().getAddress(),
                                wallet.getCurrencyId(), wallet.getNetworkId());
                                transaction = NativeDataHelper.makeTransactionETHFromKey(
                                keyObject.getPrivateKey(),
                                wallet.getCurrencyId(),
                                wallet.getNetworkId(),
                                wallet.getActiveAddress().getAmountString(),
                                receiver.getAmount(),
                                receiver.getAddress(),
                                estimation == null ? Constants.GAS_LIMIT_DEFAULT : estimation,
                                feeRate,
                                wallet.getEthWallet().getNonce());
                    } else {
                        transaction = NativeDataHelper.makeTransactionETH(
                                seed,
                                wallet.getIndex(),
                                0,
                                wallet.getCurrencyId(),
                                wallet.getNetworkId(),
                                wallet.getActiveAddress().getAmountString(),
                                receiver.getAmount(),
                                receiver.getAddress(),
                                estimation == null ? Constants.GAS_LIMIT_DEFAULT : estimation,
                                feeRate,
                                wallet.getEthWallet().getNonce());
                    }
                }
                isHd = false;
                break;
            default:
                throw new IllegalStateException("No one currency id is not Blockchain value!");
        }

        String hex = byteArrayToHex(transaction);
        switch (NativeDataHelper.Blockchain.valueOf(wallet.getCurrencyId())) {
            case ETH:
                hex = "0x" + hex;
        }
        final HdTransactionRequestEntity entity = new HdTransactionRequestEntity(wallet.getCurrencyId(), wallet.getNetworkId(),
                new HdTransactionRequestEntity.Payload(changeAddress, wallet.getAddresses().size(),
                        wallet.getIndex(), hex, isHd));

        Timber.i("hex=%s", hex);
        Timber.i("change address=%s", changeAddress);
        MultyApi.INSTANCE.sendHdTransaction(entity).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, @NonNull Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "SHOW SUCSESS SHOULD BE CALLED");
                    showSuccess();
                } else {
                    try {
                        String errorBody = response.errorBody() == null ? "" : response.errorBody().string();
                        showError(new IllegalStateException(errorBody));
                        if (response.code() == 406) {
                            Analytics.getInstance(Multy.getContext()).logEvent(getClass().getSimpleName(), "406", errorBody);
                        }
                    } catch (IOException e) {
                        showError(new IllegalStateException(""));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                showError((Exception) t);
            }
        });

        return false;
    }

    private void showSuccess() {
//        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
//            socketManager.disconnect();
//        }
//        socketManager.lazyDisconnect();
        Log.d(TAG, "SHOW SUCSESS INSIDE CALL CALLED");
        Analytics.getInstance(this).logEvent(AnalyticsConstants.KF_TRANSACTION_SUCCESS, AnalyticsConstants.KF_TRANSACTION_SUCCESS, String.valueOf(true));

        LottieAnimationView animationView = receiversPagerAdapter.getAnimationView(pagerRequests.getCurrentItem());

        handler.postDelayed(() -> new CompleteDialogFragment().show(getSupportFragmentManager(), ""), 1500);

//        if (animationView != null && animationView.isAnimating()) {
//            Log.d(TAG, "SHOW SUCSESS INSIDE CALL ANIMATION IS NOT NULL");
//            animationView.addAnimatorListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    Log.d(TAG, "SHOW SUCSESS INSIDE CALL CALL ANIMATION END");
//                    new CompleteDialogFragment().show(getSupportFragmentManager(), "");
//                }
//            });
//        } else {
//            Log.d(TAG, "SHOW SUCSESS INSIDE CALL CALL WITHOUT ANIMATION");
//            new CompleteDialogFragment().show(getSupportFragmentManager(), "");
//        }
    }

    private void logError(Exception e) {
        Analytics.getInstance(this).logEvent(AnalyticsConstants.KF_TRANSACTION_ERROR, AnalyticsConstants.KF_TRANSACTION_ERROR, e == null ? "Unknown error" : e.getMessage());
    }

    private void showError(Exception e) {
        logError(e);
        if (e.getMessage().contains("Transaction is trying to spend more than available") ||
                e.getMessage().contains("insufficient funds")) {
            showError(getString(R.string.not_enough_balance));
        } else {
            if (e.getMessage().contains("nonce too low")) {
                updateLocalWallets();
            }
            showError(getString(R.string.error_sending_tx));
        }
        e.printStackTrace();
        Crashlytics.logException(e);
    }

    private void showError(String message) {
        AlertDialog dialog = new AlertDialog.Builder(MagicSendActivity.this)
                .setTitle(TextUtils.isEmpty(message) ? getString(R.string.error_sending_tx) : message)
                .setPositiveButton(R.string.ok, (dialog1, which) -> dialog1.dismiss())
                .setCancelable(false)
                .create();
        dialog.show();
    }

    private void bringViewsBack() {
        final float yTo = textScanning.getY();
        hideSendState();
        containerSend.animate().translationYBy(yTo).alpha(1).setDuration(0).start();
    }

    private void animateSend() {
        final float yTo = textScanning.getY();
        containerSend.animate().translationY(-yTo)
                .withEndAction(() -> {
                    containerSend.setVisibility(View.INVISIBLE);
                    bringViewsBack();
                    receiversPagerAdapter.showElements(pagerRequests.getCurrentItem());
                    walletPagerAdapter.showElements(pagerWallets.getCurrentItem());
                    enableScroll();

                    final Wallet wallet = RealmManager.getAssetsDao().getWalletById(walletPagerAdapter.getSelectedWalletId(pagerWallets.getCurrentItem()));
                    Callback<FeeRateResponse> callback = new Callback<FeeRateResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<FeeRateResponse> call, @NonNull Response<FeeRateResponse> response) {
                            try {
                                if (wallet.isMultisig()) {
                                    getEstimation(wallet, String.valueOf((response.body()).getSpeeds().getFast()));
                                } else {
                                    send(wallet, String.valueOf((response.body()).getSpeeds().getFast()), response.body().getCustomGasLimit());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                showError(e);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<FeeRateResponse> call, @NonNull Throwable t) {
                            t.printStackTrace();
                            showError(new Exception(t));
                        }
                    };
                    if (wallet.getCurrencyId() == NativeDataHelper.Blockchain.ETH.getValue()) {
                        MultyApi.INSTANCE.getFeeRates(wallet.getCurrencyId(), wallet.getNetworkId(),
                                receiversPagerAdapter.getReceiver(pagerRequests.getCurrentItem()).getAddress()).enqueue(callback);
                    } else {
                        MultyApi.INSTANCE.getFeeRates(wallet.getCurrencyId(), wallet.getNetworkId()).enqueue(callback);
                    }

                    receiversPagerAdapter.showSuccess(pagerRequests.getCurrentItem());
                }).setDuration(500).alpha(0).setInterpolator(new AnticipateOvershootInterpolator()).start();
    }

    private void getEstimation(Wallet multisigWallet, String gasPrice) {
        MultyApi.INSTANCE.getEstimations(multisigWallet.getActiveAddress().getAddress()).enqueue(new Callback<Estimation>() {
            @Override
            public void onResponse(@NonNull Call<Estimation> call, @NonNull Response<Estimation> response) {
                Estimation body = response.body();
                try {
                    send(multisigWallet, gasPrice, body.getSubmitTransaction());
                } catch (Exception e) {
                    logError(e);
                    showError("");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Estimation> call, @NonNull Throwable t) {
                t.printStackTrace();
                logError(new Exception(t));
                showError("");
            }
        });
    }

    private void grantBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            return;
        }

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(enableLocationIntent, REQUEST_CODE_LOCATION);
        }
    }

    private void start() {
        startSockets();
    }

    private void stop() {
        if (mBluetoothService != null && mBluetoothService.serviceMode == BluetoothService.BluetoothServiceMode.SCANNER) {
            stopBleScanner();
        }

//        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
//            socketManager.disconnect();
//            handler.removeCallbacksAndMessages(null);
//        }

        socketManager.lazyDisconnect(TAG);
        handler.removeCallbacksAndMessages(null);
    }

    private void startBleScanner() {
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            if (mBluetoothService != null && mBluetoothService.serviceMode != BluetoothService.BluetoothServiceMode.SCANNER) {
                mBluetoothService.startScan();
            }
        } else {
            initPermissions();
        }
    }

    private void stopBleScanner() {
        if (mBluetoothService.serviceMode == BluetoothService.BluetoothServiceMode.SCANNER) {
            mBluetoothService.stopScan();
        }
    }

    private JSONObject getIdsJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (String id : leIds) {
                jsonArray.put(id);
            }
            jsonObject.put("ids", jsonArray);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Show Location");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                String message = getString(R.string.permissions_for_access_nearby_devices);
                showDialog(message,
                        (dialog, which) -> requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS));
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

        grantBluetooth();
    }

    private void showDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.yes), okListener)
                .setNegativeButton(getString(R.string.cancel), (dialog, whitch) -> onBackPressed())
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }


    private void emitUpdateReceivers() {
        //TODO rewrite it!!!
        try {

            socketManager.sendEvent(EVENT_SENDER_CHECK, getIdsJson(), (Ack) args -> MagicSendActivity.this.runOnUiThread(() -> {
                int prevNetworkId = -1;
                int prevCurrencyId = -1;

                if (receiversPagerAdapter.getCount() != 0) {
                    FastReceiver fastReceiver = receiversPagerAdapter.getReceiver(pagerRequests.getCurrentItem());
                    if (fastReceiver != null) {
                        prevCurrencyId = fastReceiver.getCurrencyId();
                        prevNetworkId = fastReceiver.getNetworkId();
                    }
                }

                String json = args[0].toString();
                FastReceiver[] receivers = new Gson().fromJson(json, FastReceiver[].class);
                ArrayList<FastReceiver> list = new ArrayList(Arrays.asList(receivers));

                if (receivers != null && receiversPagerAdapter != null) {
                    receiversPagerAdapter.setData(list);
                }

                if (receiversPagerAdapter.getCount() != 0) {
                    FastReceiver fastReceiver = receiversPagerAdapter.getReceiver(pagerRequests.getCurrentItem());
                    if (fastReceiver != null) {
                        if (prevCurrencyId != fastReceiver.getCurrencyId() || prevNetworkId != fastReceiver.getNetworkId()) {
                            initPagerWallets();
                        }
                    }
                }

                if (selectedWalletId != -1) {
                    enableScroll();
                    showPagerElements();
                }

            }));



//            socketManager.getSocket().emit(EVENT_SENDER_CHECK, getIdsJson(), (Ack) args -> MagicSendActivity.this.runOnUiThread(() -> {
//                int prevNetworkId = -1;
//                int prevCurrencyId = -1;
//
//                if (receiversPagerAdapter.getCount() != 0) {
//                    FastReceiver fastReceiver = receiversPagerAdapter.getReceiver(pagerRequests.getCurrentItem());
//                    if (fastReceiver != null) {
//                        prevCurrencyId = fastReceiver.getCurrencyId();
//                        prevNetworkId = fastReceiver.getNetworkId();
//                    }
//                }
//
//                String json = args[0].toString();
//                FastReceiver[] receivers = new Gson().fromJson(json, FastReceiver[].class);
//                ArrayList<FastReceiver> list = new ArrayList(Arrays.asList(receivers));
//
//                if (receivers != null && receiversPagerAdapter != null) {
//                    receiversPagerAdapter.setData(list);
//                }
//
//                if (receiversPagerAdapter.getCount() != 0) {
//                    FastReceiver fastReceiver = receiversPagerAdapter.getReceiver(pagerRequests.getCurrentItem());
//                    if (fastReceiver != null) {
//                        if (prevCurrencyId != fastReceiver.getCurrencyId() || prevNetworkId != fastReceiver.getNetworkId()) {
//                            initPagerWallets();
//                        }
//                    }
//                }
//
//                if (selectedWalletId != -1) {
//                    enableScroll();
//                    showPagerElements();
//                }
//
//            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUpdateAction() {
        updateReceiversAction = () -> {
            emitUpdateReceivers();
            handler.postDelayed(updateReceiversAction, UPDATE_PERIOD);
        };
    }

    @OnClick(R.id.button_qr)
    public void onClickQr() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST_CODE);
        } else {
            startActivityForResult(new Intent(this, ScanActivity.class), Constants.CAMERA_REQUEST_CODE);
        }
    }

    @OnClick(R.id.button_cancel)
    public void onClickCancel() {
        onBackPressed();
    }
}
