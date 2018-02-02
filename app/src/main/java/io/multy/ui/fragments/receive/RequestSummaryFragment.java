/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.receive;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.DeepLinkShareHelper;
import io.multy.util.NumberFormatter;
import io.multy.viewmodels.AssetRequestViewModel;


public class RequestSummaryFragment extends BaseFragment {

    public static RequestSummaryFragment newInstance(){
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);
        setBaseViewModel(viewModel);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_summary, container, false);
        ButterKnife.bind(this, view);
        if (getActivity().getIntent().hasExtra(Constants.EXTRA_WALLET_ID)) {
            containerWallet.setEnabled(false);
        }

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        textAddress.setText(viewModel.getWalletAddress());
        textWalletName.setText(viewModel.getWallet().getName());

        double balance = viewModel.getWallet().calculateBalance();
        double pending = viewModel.getWallet().getPendingBalance() + balance;

        final double pendingBalance = pending / Math.pow(10, 8);

        textBalanceCurrency.setText(pending == 0 ? "0.0$" : NumberFormatter.getInstance().format(viewModel.getExchangePrice() * pendingBalance) + "$");
        textBalanceOriginal.setText(pending != 0 ? CryptoFormatUtils.satoshiToBtc(pending) : String.valueOf(pending));
        textBalanceOriginal.append(Constants.SPACE);
        textBalanceOriginal.append(CurrencyCode.BTC.name());

        if (viewModel.getAmount() != zero){
            textRequestAmount.setVisibility(View.INVISIBLE);
            textBalanceCurrencySend.setVisibility(View.VISIBLE);
            textBalanceOriginalSend.setVisibility(View.VISIBLE);
            textBalanceCurrencySend.setText(NumberFormatter.getInstance().format(viewModel.getAmount() * viewModel.getExchangePrice()));
            textBalanceCurrencySend.append(Constants.SPACE);
            textBalanceCurrencySend.append(CurrencyCode.USD.name());
            textBalanceOriginalSend.setText(NumberFormatter.getInstance().format(viewModel.getAmount()));
            textBalanceOriginalSend.append(Constants.SPACE);
            textBalanceOriginalSend.append(CurrencyCode.BTC.name());
        }

        generateQR();
    }

    @OnClick(R.id.image_qr)
    void onClickQR(){
        String address = viewModel.getWalletAddress();
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(address, address);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), R.string.address_copied, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.text_address)
    void onClickAddress(){
        ((AssetRequestActivity) getActivity()).setFragment(R.string.all_addresses,
                AddressesFragment.newInstance(viewModel.getWallet().getWalletIndex()));
    }

    @OnClick(R.id.container_summ)
    void onClickRequestAmount(){
        ((AssetRequestActivity) getActivity()).setFragment(R.string.receive_amount, AmountChooserFragment.newInstance());
    }

    @OnClick(R.id.container_wallet)
    void onClickWallet(){
        ((AssetRequestActivity) getActivity()).setFragment(R.string.receive, WalletChooserFragment.newInstance());
    }

    @OnClick(R.id.button_generate_address)
    void onClickGenerateAddress(){
        viewModel.addAddress();
        viewModel.getAddress().observe(this, address -> {
            textAddress.setText(address);
            generateQR();
        });
    }

    @OnClick(R.id.button_address)
    void onClickAddressBook(){
        Toast.makeText(getActivity(), R.string.not_implemented, Toast.LENGTH_SHORT).show();
        viewModel.addAddress();
        viewModel.getAddress().observe(this, address -> {
            textAddress.setText(address);
            generateQR();
        });
        Log.i("wise", "generated");
    }

    @OnClick(R.id.button_scan_wireless)
    void onClickWirelessScan(){
        Toast.makeText(getActivity(), R.string.not_implemented, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_options)
    void onClickOptions(){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                DeepLinkShareHelper.getDeepLink(getActivity(), viewModel.getStringQr()));
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.send_via)));
    }

    private void generateQR(){
        new Thread(() -> getActivity().runOnUiThread(() -> {
            final Bitmap bitmap;
            try {
                bitmap = viewModel.generateQR(getActivity());
                imageQr.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        })).start();
    }

}
