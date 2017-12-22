/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.ui.activities.MainActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NativeDataHelper;
import io.multy.util.NumberFormatter;
import io.multy.viewmodels.AssetSendViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendSummaryFragment extends BaseFragment {

    private static final String TAG = SendSummaryFragment.class.getSimpleName();

    @BindView(R.id.text_receiver_balance_original)
    TextView textReceiverBalanceOriginal;
    @BindView(R.id.text_receiver_balance_currency)
    TextView textReceiverBalanceCurrency;
    @BindView(R.id.text_receiver_address)
    TextView textReceiverAddress;
    @BindView(R.id.text_wallet_name)
    TextView textWalletName;
    @BindView(R.id.text_sender_balance_original)
    TextView textSenderBalanceOriginal;
    @BindView(R.id.text_sender_balance_currency)
    TextView textSenderBalanceCurrency;
    @BindView(R.id.text_fee_speed)
    TextView textFeeSpeed;
    @BindView(R.id.text_fee_amount)
    TextView textFeeAmount;

    @BindString(R.string.donation_format_pattern)
    String formatPattern;
    @BindString(R.string.donation_format_pattern_bitcoin)
    String formatPatternBitcoin;

    private AssetSendViewModel viewModel;

    public static SendSummaryFragment newInstance() {
        return new SendSummaryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_summary, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        setInfo();
        return view;
    }

    @OnClick(R.id.button_next)
    void onClickNext() {
//        AssetSendDialogFragment dialog = new AssetSendDialogFragment();
//        dialog.show(getActivity().getFragmentManager(), null);

        double amount = viewModel.getAmount();
        long amountSatoshi = (long) (amount * Math.pow(10, 8));
        String addressTo = viewModel.getReceiverAddress().getValue();

        try {
            byte[] transactionHex = NativeDataHelper.makeTransaction(new DataManager(Multy.getContext()).getSeed().getSeed(), 0, String.valueOf(amountSatoshi), "2000", "20000", addressTo);
            String hex = byteArrayToHex(transactionHex);
            Log.i(TAG, "hex= " + hex);

            MultyApi.INSTANCE.sendRawTransaction(hex, 1).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    startActivity(new Intent(getActivity(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private void setInfo() {
        textReceiverBalanceOriginal.setText(NumberFormatter.getInstance().format(viewModel.getAmount()));
        textReceiverBalanceOriginal.append(Constants.SPACE);
        textReceiverBalanceOriginal.append(CurrencyCode.BTC.name());
        textReceiverBalanceCurrency.setText(NumberFormatter.getInstance().format(viewModel.getAmount() * viewModel.getExchangePrice().getValue()));
        textReceiverBalanceCurrency.append(Constants.SPACE);
        textReceiverBalanceCurrency.append(CurrencyCode.USD.name());
//        textReceiverAddress.setText(viewModel.getReceiverAddress().getValue());
        textReceiverAddress.setText(viewModel.thoseAddress.getValue());
        textWalletName.setText(viewModel.getWallet().getName());
        double balance = viewModel.getWallet().getBalance();
        textSenderBalanceOriginal.setText(balance != 0 ? CryptoFormatUtils.satoshiToBtc(balance) : String.valueOf(balance));
        textSenderBalanceCurrency.setText(viewModel.getWallet().getBalanceFiatWithCode(viewModel.getExchangePrice().getValue(), CurrencyCode.USD));
        textFeeSpeed.setText(viewModel.getFee().getTime());
        textFeeAmount.setText(String.valueOf(viewModel.getFee().getCost()));
    }

}
