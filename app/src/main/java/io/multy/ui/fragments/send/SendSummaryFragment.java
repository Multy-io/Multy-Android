/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.viewmodels.AssetSendViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendSummaryFragment extends BaseFragment {

    public static SendSummaryFragment newInstance() {
        return new SendSummaryFragment();
    }

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

    private AssetSendViewModel viewModel;

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

        //TODO get outputs
        Call<ResponseBody> responseBodyCall = MultyApi.INSTANCE.getSpendableOutputs(0);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i("wise", "onResponse ");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });


        DataManager dataManager = new DataManager(getActivity());
        int amount = (int) viewModel.getAmount();
        String addressTo = viewModel.getReceiverAddress().getValue();
        String addressFrom = viewModel.getWallet().getCreationAddress();
        String txHash = "";
        String pubKey = "";
        String sum = "";
        byte[] seed = dataManager.getSeed().getSeed();
        int outIndex = 0;
        try {
            byte[] transactionHex = NativeDataHelper.makeTransaction(seed, txHash, pubKey, outIndex, sum, String.valueOf(amount), "1", addressTo, addressFrom);
            String hex = byteArrayToHex(transactionHex);
            Log.i("wise", "hex=" + hex);

            //TODO send hex
        } catch (JniException e) {
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
        textReceiverBalanceOriginal.setText(String.valueOf(viewModel.getAmount()));
        textReceiverBalanceCurrency.setText(String.valueOf(viewModel.getAmount()));
        textReceiverAddress.setText(viewModel.getReceiverAddress().getValue());
        textWalletName.setText(viewModel.getWallet().getName());
        textSenderBalanceOriginal.setText(viewModel.getWallet().getBalanceWithCode(CurrencyCode.BTC));
        textSenderBalanceCurrency.setText(viewModel.getWallet().getBalanceFiatWithCode(viewModel.getExchangePrice().getValue(), CurrencyCode.USD));
        textFeeSpeed.setText(viewModel.getFee().getTime());
        textFeeAmount.setText(String.valueOf(viewModel.getFee().getCost()));
    }

}
