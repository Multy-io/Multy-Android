/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.receive;

import android.app.Activity;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.socket.SocketManager;
import io.multy.api.socket.TransactionUpdateResponse;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.socket.ReceiveMessage;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.activities.FastReceiveActivity;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.asset.AssetInfoFragment;
import io.multy.ui.fragments.dialogs.AddressActionsDialogFragment;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
import io.multy.util.Constants;
import io.multy.util.DeepLinkShareHelper;
import io.multy.util.NativeDataHelper;
import io.multy.util.NumberFormatter;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetRequestViewModel;
import timber.log.Timber;


public class RequestSummaryFragment extends BaseFragment {
    public static final String TAG = RequestSummaryFragment.class.getSimpleName();
    public static final int AMOUNT_CHOOSE_REQUEST = 729;
    public static final int ADDRESS_CHOOSER_REQUEST = 560;
    public static final int REQUEST_CODE_WIRELESS = 112;

    public static RequestSummaryFragment newInstance() {
        return new RequestSummaryFragment();
    }

    @BindView(R.id.image_qr)
    ImageView imageQr;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.text_balance_original)
    TextView textBalanceOriginal;
    @BindView(R.id.text_balance_currency)
    TextView textBalanceCurrency;
    @BindView(R.id.text_balance_original_send)
    TextView textBalanceOriginalSend;
    @BindView(R.id.text_balance_currency_send)
    TextView textBalanceCurrencySend;
    @BindView(R.id.button_request_amount)
    TextView textRequestAmount;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.text_wallet_name)
    TextView textWalletName;
    @BindView(R.id.container_wallet)
    ConstraintLayout containerWallet;
    @BindView(R.id.button_start_broadcast)
    View buttonBroadcast;

    private AssetRequestViewModel viewModel;
    private SharingBroadcastReceiver receiver;
//    private SocketManager socketManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);
        setBaseViewModel(viewModel);
        receiver = new SharingBroadcastReceiver();
        Analytics.getInstance(getActivity()).logReceiveSummaryLaunch(viewModel.getChainId());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_summary, container, false);
        ButterKnife.bind(this, view);
        viewModel.getAddress().observe(this, address -> {
            textAddress.setText(address);
            generateQR();
        });
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
//        connectSockets();

        if (getActivity() != null) {
            getActivity().registerReceiver(receiver, new IntentFilter());
        }
        textWalletName.setText(viewModel.getWallet().getWalletName());
        textBalanceCurrency.setText(viewModel.getWallet().getFiatBalanceLabel());
        textBalanceOriginal.setText(viewModel.getWallet().getBalanceLabel());
        if (viewModel.getAmount() != 0) {
            setBalance();
        }
        generateQR();

//        if (viewModel.getWallet().getCurrencyId() != NativeDataHelper.Blockchain.BTC.getValue()) {
//            buttonBroadcast.setVisibility(View.GONE);
//        } else {
//            buttonBroadcast.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.unsubscribeSockets(TAG);

        if (getActivity() != null) {
            try {
                getActivity().unregisterReceiver(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        viewModel.subscribeToSockets(TAG);
        subscribeToUpdates();
    }

    private void subscribeToUpdates(){
        viewModel.subscribeToReceive();
        viewModel.getReceiveValue().observe(this, msg ->{
            verifyTransaction(msg);
        });
        viewModel.getReceiveMessage().observe(this, msg ->{
            verifyTransaction(msg);
        });
    }



//    private void connectSockets() {
//        socketManager = SocketManager.getInstance();
////        socketManager.listenEvent(SocketManager.EVENT_RECEIVE, args -> {
////            if (getActivity() != null) {
////                getActivity().runOnUiThread(() -> {
////                    try {
//                        verifyTransaction(args[0].toString());
////                    } catch (Throwable t) {
////                        t.printStackTrace();
////                        Crashlytics.logException(t);
////                    }
////                });
////            }
////        });
////        socketManager.listenEvent(SocketManager.getEventReceive(RealmManager.getSettingsDao().getUserId().getUserId()), args -> {
////            if (getActivity() != null) {
////                getActivity().runOnUiThread(() -> {
////                    try {
////                        verifyTransaction(new Gson().fromJson(args[0].toString(), ReceiveMessage.class));
////                    } catch (Throwable t) {
////                        t.printStackTrace();
////                    }
////                });
////            }
////        });
////        socketManager.connect(TAG);
//    }

    private void verifyTransaction(String json) {
        Timber.i("got transaction " + json);
        TransactionUpdateResponse response = new Gson().fromJson(json, TransactionUpdateResponse.class);
        if (response != null) {
            final String address = viewModel.getWallet().getActiveAddress().getAddress();
            if (response.getEntity().getAddress().equals(address) && response.getEntity().getType() == Constants.TX_MEMPOOL_INCOMING) {
                CompleteDialogFragment.newInstance(viewModel.getWallet().getCurrencyId()).show(getActivity().getSupportFragmentManager(), "");
            }
        }
    }

    private void verifyTransaction(ReceiveMessage receiveMessage) {
        final String address = viewModel.getWallet().getActiveAddress().getAddress();
        final ReceiveMessage.Payload payload = receiveMessage.getPayload();
        if (payload.getTo().equals(address) && (receiveMessage.getType() == Constants.EVENT_TYPE_NOTIFY_PAYMENT_REQUEST ||
                receiveMessage.getType() == Constants.EVENT_TYPE_NOTIFY_INCOMING_TX)) {
            CompleteDialogFragment.newInstance(viewModel.getWallet().getCurrencyId()).show(getActivity().getSupportFragmentManager(), "");
        }
    }

//    private void disconnectSockets() {
//        SocketManager.getInstance().lazyDisconnect(TAG);
////        if(socketManager != null && socketManager.isConnected()) {
////            socketManager.disconnect();
////        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AMOUNT_CHOOSE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(Constants.EXTRA_AMOUNT)) {
                double amount = data.getDoubleExtra(Constants.EXTRA_AMOUNT, 0);
                viewModel.setAmount(amount);
                setBalance();
            }
        } else if (requestCode == ADDRESS_CHOOSER_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(Constants.EXTRA_ADDRESS)) {
                viewModel.setAddress(data.getStringExtra(Constants.EXTRA_ADDRESS));
            }
        } else if (requestCode == REQUEST_CODE_WIRELESS && resultCode == Activity.RESULT_OK) {
            getActivity().finish();
        }
    }

    private void generateQR() {
        imageQr.setImageBitmap(null);
        String strQr = viewModel.getStringQr();
        progressBar.setVisibility(View.VISIBLE);
        viewModel.generateQr(strQr, getResources().getColor(android.R.color.black),
                getResources().getColor(android.R.color.white), bitmap -> {
                    imageQr.setImageBitmap(bitmap);
                    progressBar.setVisibility(View.GONE);
                }, throwable -> viewModel.errorMessage.setValue(throwable.getLocalizedMessage()));
    }

    private void setBalance() {
        textRequestAmount.setVisibility(View.INVISIBLE);
        textBalanceCurrencySend.setVisibility(View.VISIBLE);
        textBalanceOriginalSend.setVisibility(View.VISIBLE);
        textBalanceCurrencySend.setText(NumberFormatter.getFiatInstance().format(viewModel.getAmount() * viewModel.getExchangePrice()));
        textBalanceCurrencySend.append(Constants.SPACE);
        textBalanceCurrencySend.append(CurrencyCode.USD.name());
        textBalanceOriginalSend.setText(NumberFormatter.getInstance().format(viewModel.getAmount()));
        textBalanceOriginalSend.append(Constants.SPACE);
        textBalanceOriginalSend.append(viewModel.getWallet().getCurrencyName());
    }

    private void copyAddressToClipboard() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_ADDRESS, viewModel.getWallet().getCurrencyId());
        String address = viewModel.getWallet().getActiveAddress().getAddress();
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }
        ClipData clip = ClipData.newPlainText(address, address);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), R.string.address_copied, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.image_qr)
    void onClickQR() {
        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_QR, viewModel.getChainId());
        copyAddressToClipboard();
    }

    @OnClick(R.id.text_address)
    void onClickAddress() {
        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_ADDRESS, viewModel.getChainId());
        switch (NativeDataHelper.Blockchain.valueOf(viewModel.getWallet().getCurrencyId())) {
            case BTC:
                AddressesFragment fragment = AddressesFragment.newInstance(viewModel.getWallet().getId());
                fragment.setTargetFragment(this, ADDRESS_CHOOSER_REQUEST);
                ((AssetRequestActivity) getActivity()).setFragment(R.string.all_addresses, fragment);
                break;
            case ETH:
                AddressActionsDialogFragment.getInstance(viewModel.getWallet().getActiveAddress().getAddress(),
                        viewModel.getWalletLive().getValue().getCurrencyId(), viewModel.getWalletLive().getValue().getNetworkId(),
                        viewModel.getWalletLive().getValue().getIconResourceId(), false)
                .show(getChildFragmentManager(), AddressActionsDialogFragment.TAG);
                break;
        }
    }

    @OnClick(R.id.container_summ)
    void onClickRequestAmount() {
        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_REQUEST_SUM, viewModel.getChainId());
        if (getActivity() instanceof AssetRequestActivity) {
            AmountChooserFragment fragment = AmountChooserFragment.newInstance();
            fragment.setTargetFragment(this, AMOUNT_CHOOSE_REQUEST);
            ((AssetRequestActivity) getActivity()).setFragment(R.string.receive_amount, fragment);
        }
    }

    @OnClick(R.id.container_wallet)
    void onClickWallet() {
        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_CHANGE_WALLET, viewModel.getChainId());
        if (getActivity() instanceof AssetRequestActivity) {
            ((AssetRequestActivity) getActivity()).setFragment(R.string.receive, WalletChooserFragment.newInstance());
        }
    }

    @OnClick(R.id.button_generate_address)
    void onClickGenerateAddress() {
        viewModel.getBtcAddresses();
        viewModel.getAddress().observe(this, address -> {
            viewModel.setAddress(address);
        });
    }

//    @OnClick(R.id.button_address)
//    void onClickAddressBook(View v) {
//        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_ADDRESS_BOOK, viewModel.getChainId());
//        v.setEnabled(false);
//        v.postDelayed(() -> v.setEnabled(true), 500);
//        if (getActivity() != null) {
//            DonateDialog.getInstance(Constants.DONATE_ADDING_CONTACTS).show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
//        }
//    }

//    @OnClick(R.id.button_scan_wireless)
//    void onClickWirelessScan() {
//        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_WIRELESS, viewModel.getChainId());
//        if (getActivity() != null) {
//            DonateDialog.getInstance(Constants.DONATE_ADDING_WIRELESS_SCAN).show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
//        }
//    }

    @OnClick(R.id.button_options)
    void onClickOptions() {
        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_OPTIONS, viewModel.getChainId());
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                DeepLinkShareHelper.getDeepLink(getActivity(), viewModel.getStringAddress(), viewModel.getStringAmount()));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Intent intentReceiver = new Intent(getActivity(), AssetInfoFragment.SharingBroadcastReceiver.class);
            intentReceiver.putExtra(getString(R.string.chain_id), viewModel.getChainId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intentReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share), pendingIntent.getIntentSender()));
        } else {
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share)));
        }
    }

    @OnClick(R.id.button_start_broadcast)
    public void onStartBroadcast() {
        if (viewModel.getAmount() == 0) {
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
            animation.setFillAfter(false);
            textRequestAmount.startAnimation(animation);
        } else {
            Intent intent = new Intent(getActivity(), FastReceiveActivity.class);
            intent.putExtra(Constants.EXTRA_WALLET_ID, viewModel.getWallet().getId());
            intent.putExtra(Constants.EXTRA_AMOUNT, viewModel.getAmount());
            startActivityForResult(intent, REQUEST_CODE_WIRELESS);
        }
    }

    public static class SharingBroadcastReceiver extends BroadcastReceiver {

        public SharingBroadcastReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null && intent.getExtras().get(Intent.EXTRA_CHOSEN_COMPONENT) != null &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                String component = intent.getExtras().get(Intent.EXTRA_CHOSEN_COMPONENT).toString();
                String packageName = component.substring(component.indexOf("{") + 1, component.indexOf("/"));
                Analytics.getInstance(context).logWalletSharing(intent.getIntExtra(context.getString(R.string.chain_id), 1), packageName);
            }
        }
    }
}
