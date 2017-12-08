/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.receive;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.util.Linkify;
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
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.DeepLinkShareHelper;
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

    @BindInt(R.integer.zero)
    int zero;

    private AssetRequestViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_summary, container, false);
        ButterKnife.bind(this, view);

        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        textAddress.setText(viewModel.getWallet().getCreationAddress());
        textWalletName.setText(viewModel.getWallet().getName());
        textBalanceOriginal.setText(viewModel.getWallet().getBalanceWithCode(CurrencyCode.BTC));
        textBalanceCurrency.setText(viewModel.getWallet().getBalanceFiatWithCode(viewModel.getExchangePrice().getValue(), CurrencyCode.USD));

        if (viewModel.getAmount() != zero){
            textRequestAmount.setVisibility(View.INVISIBLE);
            textBalanceCurrencySend.setVisibility(View.VISIBLE);
            textBalanceOriginalSend.setVisibility(View.VISIBLE);
            textBalanceCurrencySend.setText(String.valueOf(viewModel.getAmount()));
            textBalanceOriginalSend.setText(String.valueOf(viewModel.getAmount()));
        }

        new Thread(() -> {
            try {
                final Bitmap bitmap = viewModel.generateQR(getActivity());
                getActivity().runOnUiThread(() -> imageQr.setImageBitmap(bitmap));
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @OnClick(R.id.button_request_amount)
    void onClickRequestAmount(){
        ((AssetRequestActivity) getActivity()).setFragment(R.string.receive, AmountChooserFragment.newInstance());
    }

    @OnClick(R.id.text_balance_original_send)
    void onClickBalanceOriginalSendAmount(){
        ((AssetRequestActivity) getActivity()).setFragment(R.string.receive, AmountChooserFragment.newInstance());
    }

    @OnClick(R.id.text_balance_currency_send)
    void onClickBalanceUsdSendAmount(){
        ((AssetRequestActivity) getActivity()).setFragment(R.string.receive, AmountChooserFragment.newInstance());
    }

    @OnClick(R.id.text_balance_currency)
    void onClickBalanceUsdAmount(){
        getFragmentManager().popBackStack();
    }

    @OnClick(R.id.text_wallet_name)
    void onClickWalletName(){
        Linkify.addLinks(textWalletName, Linkify.WEB_URLS);
    }

    @OnClick(R.id.button_options)
    void onClickOptions(){
        Toast.makeText(getActivity(), DeepLinkShareHelper.getDeepLink(getActivity(),
                viewModel.getQr()), Toast.LENGTH_LONG ).show();
    }
}
