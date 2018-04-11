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
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import java.math.BigInteger;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.BtcWallet;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.ui.activities.AmountChooserActivity;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.asset.AssetInfoFragment;
import io.multy.ui.fragments.dialogs.DonateDialog;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.DeepLinkShareHelper;
import io.multy.util.NumberFormatter;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetRequestViewModel;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class RequestSummaryFragment extends BaseFragment {

    public static final int AMOUNT_CHOOSE_REQUEST = 729;

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

    @BindInt(R.integer.zero)
    int zero;

    private AssetRequestViewModel viewModel;
    private SharingBroadcastReceiver receiver;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_summary, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().registerReceiver(receiver, new IntentFilter());
        }

        textAddress.setText(viewModel.getWalletAddress());
        textWalletName.setText(viewModel.getWallet().getWalletName());
        textBalanceCurrency.setText(viewModel.getWallet().getFiatBalanceLabel());
        textBalanceOriginal.setText(viewModel.getWallet().getBalanceLabel());

        if (viewModel.getAmount() != zero) {
            setBalance();
        }

        generateQR();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null) {
            try {
                getActivity().unregisterReceiver(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.image_qr)
    void onClickQR() {
        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_QR, viewModel.getChainId());
        String address = viewModel.getWalletAddress();
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(address, address);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), R.string.address_copied, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.text_address)
    void onClickAddress() {
        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_ADDRESS, viewModel.getChainId());
        ((AssetRequestActivity) getActivity()).setFragment(R.string.all_addresses,
                AddressesFragment.newInstance(viewModel.getWallet().getId()));
    }

    @OnClick(R.id.container_summ)
    void onClickRequestAmount() {
        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_REQUEST_SUM, viewModel.getChainId());
//        ((AssetRequestActivity) getActivity()).setFragment(R.string.receive_amount, AmountChooserFragment.newInstance());
        startActivityForResult(new Intent(getContext(), AmountChooserActivity.class).putExtra(Constants.EXTRA_AMOUNT, viewModel.getAmount()), AMOUNT_CHOOSE_REQUEST);
    }

    @OnClick(R.id.container_wallet)
    void onClickWallet() {
        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_CHANGE_WALLET, viewModel.getChainId());
        ((AssetRequestActivity) getActivity()).setFragment(R.string.receive, WalletChooserFragment.newInstance());
    }

    @OnClick(R.id.button_generate_address)
    void onClickGenerateAddress() {
        viewModel.getBtcAddresses();
        viewModel.getAddress().observe(this, address -> {
            textAddress.setText(address);
            generateQR();
        });
    }

    @OnClick(R.id.button_address)
    void onClickAddressBook(View v) {
        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_ADDRESS_BOOK, viewModel.getChainId());
//        Toast.makeText(getActivity(), R.string.not_implemented, Toast.LENGTH_SHORT).show();
//        viewModel.getBtcAddresses();
//        viewModel.getAddress().observe(this, address -> {
//            textAddress.setText(address);
//            generateQR();
//        });
//        Log.i("wise", "generated");
        v.setEnabled(false);
        v.postDelayed(() -> v.setEnabled(true), 500);
        if (getActivity() != null) {
            DonateDialog.getInstance(Constants.DONATE_ADDING_CONTACTS).show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
        }
    }

    @OnClick(R.id.button_scan_wireless)
    void onClickWirelessScan() {
        Analytics.getInstance(getActivity()).logReceiveSummary(AnalyticsConstants.RECEIVE_SUMMARY_WIRELESS, viewModel.getChainId());
//        Toast.makeText(getActivity(), R.string.not_implemented, Toast.LENGTH_SHORT).show();
        if (getActivity() != null) {
            DonateDialog.getInstance(Constants.DONATE_ADDING_WIRELESS_SCAN).show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
        }
    }

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

    private void generateQR() {
        String srtQr = viewModel.getStringQr();
        io.reactivex.Observable.create((ObservableOnSubscribe<Bitmap>) e -> {
            Bitmap bitmap = null;
            try {
                bitmap = viewModel.generateQR(RequestSummaryFragment.this.getActivity(), srtQr);
            } catch (WriterException ex) {
                ex.printStackTrace();
            }
            e.onNext(bitmap);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    imageQr.setImageBitmap(bitmap);
                }, Throwable::printStackTrace);

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
        textBalanceOriginalSend.append(CurrencyCode.BTC.name());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AMOUNT_CHOOSE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(Constants.EXTRA_AMOUNT)) {
                double amount = data.getDoubleExtra(Constants.EXTRA_AMOUNT, 0);
                viewModel.setAmount(amount);
                generateQR();
                setBalance();
            }
        }
    }

    public static class SharingBroadcastReceiver extends BroadcastReceiver {

        public SharingBroadcastReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null && intent.getExtras().get(Intent.EXTRA_CHOSEN_COMPONENT) != null) {
                String component = intent.getExtras().get(Intent.EXTRA_CHOSEN_COMPONENT).toString();
                String packageName = component.substring(component.indexOf("{") + 1, component.indexOf("/"));
                Analytics.getInstance(context).logWalletSharing(intent.getIntExtra(context.getString(R.string.chain_id), 1), packageName);
            }
        }
    }
}
