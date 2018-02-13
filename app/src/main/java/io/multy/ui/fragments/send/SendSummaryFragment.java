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

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.RecentAddress;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
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
    @BindView(R.id.text_fee_speed_label)
    TextView textFeeSpeedLabel;
    @BindView(R.id.button_next)
    View buttonNext;

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
        setBaseViewModel(viewModel);
        subscribeToErrors();
        setInfo();
//        buttonNext.setOnTouchListener(new OnSlideTouchListener() {
//            @Override
//            public boolean onSlideRight() {
//                send();
//                return true;
//            }
//        });
        return view;
    }

    @OnClick(R.id.button_next)
    void onClickNext() {
        send();
    }

    private void send() {
        //        AssetSendDialogFragment dialog = new AssetSendDialogFragment();
//        dialog.show(getActivity().getFragmentManager(), null);
        viewModel.isLoading.setValue(true);

        double amount = viewModel.getAmount();
        long amountDonationSatoshi = 0;
        long amountSatoshi = (long) (amount * Math.pow(10, 8));
        if (viewModel.getDonationAmount() != null) {
            amountDonationSatoshi = (long) (Double.valueOf(viewModel.getDonationAmount()) * Math.pow(10, 8));
        }
        String addressTo = viewModel.getReceiverAddress().getValue();

        try {
            viewModel.isLoading.setValue(true);
//
            byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();
            final int addressesSize = viewModel.getWallet().getAddresses().size();
            final String changeAddress = NativeDataHelper.makeAccountAddress(seed, viewModel.getWallet().getWalletIndex(), addressesSize, NativeDataHelper.Currency.BTC.getValue());

            //TODO make fee per byte
            byte[] transactionHex = NativeDataHelper.makeTransaction(DataManager.getInstance().getSeed().getSeed(), viewModel.getWallet().getWalletIndex(), String.valueOf(amountSatoshi),
                    String.valueOf(viewModel.getFee().getAmount()), String.valueOf(amountDonationSatoshi), addressTo, changeAddress);
            Log.i(TAG, "donation amount " + amountDonationSatoshi);
            Log.i(TAG, "fee per byte " + viewModel.getFee().getAmount());
            String hex = byteArrayToHex(transactionHex);
            Log.i(TAG, "hex= " + hex);
            Log.i(TAG, "changeAddress = " + changeAddress);

            MultyApi.INSTANCE.sendHdTransaction(new HdTransactionRequestEntity(
                    NativeDataHelper.Currency.BTC.getValue(),
                    new HdTransactionRequestEntity.Payload(changeAddress, addressesSize, viewModel.getWallet().getWalletIndex(), hex))).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        viewModel.isLoading.postValue(false);
                        RealmManager.getAssetsDao().saveRecentAddress(new RecentAddress(NativeDataHelper.Currency.BTC.getValue(), addressTo));
                        new CompleteDialogFragment().show(getActivity().getSupportFragmentManager(), "");
                    } else {
                        showError();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    showError();
                }
            });

//            MultyApi.INSTANCE.addWalletAddress(new AddWalletAddressRequest(viewModel.getWallet().getWalletIndex(), changeAddress, addressesSize)).enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                    RealmManager.getAssetsDao().saveAddress(viewModel.getWallet().getWalletIndex(), new WalletAddress(addressesSize, changeAddress));
//                    //TODO REMOVE THIS HARDCODE of the currency ID from this awesome Api request
//                    MultyApi.INSTANCE.sendRawTransaction(hex, 0).enqueue(new Callback<ResponseBody>() {
//                        @Override
//                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                            viewModel.isLoading.postValue(false);
//                            new CompleteDialogFragment().show(getActivity().getSupportFragmentManager(), "");
//                        }
//
//                        @Override
//                        public void onFailure(Call<ResponseBody> call, Throwable t) {
//                            t.printStackTrace();
//                            showError();
//                        }
//                    });
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    t.printStackTrace();
//                    showError();
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
            showError();
        }
    }

    private void showError() {
        viewModel.isLoading.postValue(false);
        viewModel.errorMessage.postValue(getString(R.string.error_sending_tx));
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private void setInfo() {
        CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        textReceiverBalanceOriginal.setText(NumberFormatter.getInstance().format(viewModel.getAmount()));
        textReceiverBalanceOriginal.append(Constants.SPACE);
        textReceiverBalanceOriginal.append(CurrencyCode.BTC.name());
        textReceiverBalanceCurrency.setText(NumberFormatter.getFiatInstance().format(viewModel.getAmount() * currenciesRate.getBtcToUsd()));
        textReceiverBalanceCurrency.append(Constants.SPACE);
        textReceiverBalanceCurrency.append(CurrencyCode.USD.name());
//        textReceiverAddress.setText(viewModel.getReceiverAddress().getValue());
        textReceiverAddress.setText(viewModel.thoseAddress.getValue());
        textWalletName.setText(viewModel.getWallet().getName());
        double balance = viewModel.getWallet().getBalance();
        textSenderBalanceOriginal.setText(balance != 0 ? CryptoFormatUtils.satoshiToBtc(balance) + " BTC" : String.valueOf(balance));
        textSenderBalanceCurrency.setText(String.format("%s USD", NumberFormatter.getFiatInstance().format(viewModel.getAmount() * currenciesRate.getBtcToUsd())));
        textFeeSpeed.setText(viewModel.getFee().getName());
        textFeeSpeedLabel.setText(viewModel.getFee().getTime());
        textFeeAmount.setText(String.format("%s BTC / %s USD", CryptoFormatUtils.satoshiToBtc(viewModel.getTransactionPrice()), CryptoFormatUtils.satoshiToUsd(viewModel.getTransactionPrice())));
    }

}
