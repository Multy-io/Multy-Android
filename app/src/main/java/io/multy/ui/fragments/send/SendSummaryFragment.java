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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.DataManager;
import io.multy.model.entities.Output;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.responses.OutputsResponse;
import io.multy.ui.activities.MainActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.CryptoFormatUtils;
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


        DataManager dataManager = new DataManager(getActivity());
        double amount = viewModel.getAmount();
        String addressTo = viewModel.getReceiverAddress().getValue();
        String addressFrom = viewModel.getWallet().getCreationAddress();

        MultyApi.INSTANCE.getSpendableOutputs(1, addressFrom).enqueue(new Callback<OutputsResponse>() {
            @Override
            public void onResponse(Call<OutputsResponse> call, Response<OutputsResponse> response) {
                OutputsResponse outputsResponse = response.body();
                Output output = outputsResponse.getOutputs().get(0);

                String txHash = output.getTxId();
                String pubKey = output.getTxOutScript();
                String sum = output.getTxOutAmount();
                long amountTotal = (long) (amount * Math.pow(10, 8));
                byte[] seed = dataManager.getSeed().getSeed();
                int outIndex = output.getTxOutId();
                try {
                    byte[] transactionHex = NativeDataHelper.makeTransaction(seed, txHash, pubKey, outIndex, sum, String.valueOf(amountTotal), "9999", addressTo, addressFrom);
                    String hex = byteArrayToHex(transactionHex);
                    Log.i("wise", "hex=" + hex);

                    MultyApi.INSTANCE.sendRawTransaction(hex, 1).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            startActivity(new Intent(getActivity(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });
                } catch (JniException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<OutputsResponse> call, Throwable t) {
                Log.i("wise", "fail");
            }
        });
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
