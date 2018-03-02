/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.Fee;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.model.responses.FeeRateResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.MyFeeAdapter;
import io.multy.ui.adapters.WalletsAdapter;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.ui.fragments.dialogs.WalletChooserDialogFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.BaseViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.multy.ui.fragments.send.SendSummaryFragment.byteArrayToHex;

public class DonationFragment extends BaseFragment {

    private final static String ARG_WALLET_INDEX = "wallet_index";

    @BindView(R.id.scroll_view)
    ScrollView scrollView;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.text_wallet_name)
    TextView textWalletName;
    @BindView(R.id.input_donation)
    EditText inputDonation;
    @BindView(R.id.text_fiat)
    TextView textFiat;
    @BindString(R.string.donation_format_pattern)
    String formatPattern;

    private BaseViewModel viewModel;
    private WalletRealmObject wallet;
    private long maxValue;

    public static DonationFragment newInstance(int walletIndex) {
        DonationFragment donationFragment = new DonationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_WALLET_INDEX, walletIndex);
        donationFragment.setArguments(args);
        return donationFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_donation, container, false);
        ButterKnife.bind(this, convertView);

        viewModel = ViewModelProviders.of(this).get(BaseViewModel.class);
        inputDonation.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollDown();
            }
        });
        inputDonation.setOnClickListener(v -> scrollDown());

        pickWallet(getArguments().getInt(ARG_WALLET_INDEX));
        setupInput();
        requestRates();
        return convertView;
    }

    private void scrollDown() {
        scrollView.postDelayed(() -> {
            View lastChild = scrollView.getChildAt(scrollView.getChildCount() - 1);
            int bottom = lastChild.getBottom() + scrollView.getPaddingBottom();
            int sy = scrollView.getScrollY();
            int sh = scrollView.getHeight();
            int delta = bottom - (sy + sh);
            scrollView.smoothScrollBy(0, delta);
        }, 400);
    }

    private void pickWallet(int walletIndex) {
        wallet = RealmManager.getAssetsDao().getWalletById(walletIndex);
        maxValue = wallet.getAvailableBalance();
        textWalletName.setText(wallet.getName());
        inputDonation.setText(CryptoFormatUtils.satoshiToBtc((wallet.getAvailableBalance() / 100) * 3));
    }

    private void setAdapter(ArrayList<Fee> rates) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new MyFeeAdapter(rates, new MyFeeAdapter.OnCustomFeeClickListener() {
            @Override
            public void onClickCustomFee(long currentValue) {
                showCustomFeeDialog(currentValue);
                logTransactionFee(5);
            }

            @Override
            public void logTransactionFee(int position) {

            }
        }));
    }

    public void showCustomFeeDialog(long currentValue) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_custom_fee, null);
        dialogBuilder.setView(dialogView);

        final TextInputEditText input = dialogView.findViewById(R.id.input_custom);
        input.setText(currentValue == -1 ? String.valueOf(20) : String.valueOf(currentValue));

        dialogBuilder.setTitle(R.string.custom_fee);
        dialogBuilder.setPositiveButton(R.string.done, (dialog, whichButton) -> {
            ((MyFeeAdapter) recyclerView.getAdapter()).setCustomFee(Long.valueOf(input.getText().toString()));
//            Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_CUSTOM_SET, viewModel.getChainId());
        });
        dialogBuilder.setNegativeButton(R.string.cancel, (dialog, whichButton) -> {
//            Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_CUSTOM_CANCEL, viewModel.getChainId());
        });
        dialogBuilder.create().show();
    }

    private void requestRates() {
        viewModel.isLoading.postValue(true);
        MultyApi.INSTANCE.getFeeRates(NativeDataHelper.Currency.BTC.getValue()).enqueue(new Callback<FeeRateResponse>() {
            @Override
            public void onResponse(Call<FeeRateResponse> call, Response<FeeRateResponse> response) {
                if (response.isSuccessful()) {
                    setAdapter(response.body().getSpeeds().asListDonation());
                } else {
                    viewModel.errorMessage.postValue(Multy.getContext().getString(R.string.error_loading_rates));
                }
            }

            @Override
            public void onFailure(Call<FeeRateResponse> call, Throwable t) {
                viewModel.isLoading.postValue(false);
                viewModel.errorMessage.postValue(t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void setupInput() {
        CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        textFiat.setText(new DecimalFormat(formatPattern).format(Double.parseDouble(inputDonation.getText().toString()) * currenciesRate.getBtcToUsd()));
        textFiat.append(Constants.SPACE);
        textFiat.append(CurrencyCode.USD.name());

        inputDonation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence)) {
                    textFiat.setText(new DecimalFormat(formatPattern).format(Double.parseDouble(charSequence.toString()) * currenciesRate.getBtcToUsd()));
                    textFiat.append(Constants.SPACE);
                    textFiat.append(CurrencyCode.USD.name());
                } else {
                    textFiat.setText(Constants.SPACE);
                }
//                isDonationChanged = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void sendTransaction() {
        viewModel.isLoading.setValue(true);
        double btcValue = Double.parseDouble(inputDonation.getText().toString());
        long satoshiValue = (long) (btcValue * Math.pow(10, 8));

        if (satoshiValue > maxValue) {
            Toast.makeText(getActivity(), "Donation amount is bigger than available.", Toast.LENGTH_SHORT).show();
        }


        final byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();
        final int addressesSize = wallet.getAddresses().size();
        final String fee = String.valueOf(((MyFeeAdapter) recyclerView.getAdapter()).getSelectedFee().getAmount());
        final String receiverAddress = Prefs.getString(Constants.PREF_DONATE_ADDRESS_BTC);
        final String donationAddress = "";
        final String amount = String.valueOf(CryptoFormatUtils.btcToSatoshi(inputDonation.getText().toString()));

        try {
            final String changeAddress = NativeDataHelper.makeAccountAddress(seed, wallet.getWalletIndex(), addressesSize,
                    NativeDataHelper.Blockchain.BLOCKCHAIN_BITCOIN.getValue(),
                    NativeDataHelper.BlockchainNetType.BLOCKCHAIN_NET_TYPE_TESTNET.getValue());
            byte[] transactionHex = NativeDataHelper.makeTransaction(seed, wallet.getWalletIndex(), amount,
                    fee, "0", receiverAddress, changeAddress, donationAddress, false);

            MultyApi.INSTANCE.sendHdTransaction(new HdTransactionRequestEntity(NativeDataHelper.Currency.BTC.getValue(),
                    new HdTransactionRequestEntity.Payload(changeAddress, addressesSize, wallet.getWalletIndex(), byteArrayToHex(transactionHex)))).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        viewModel.isLoading.postValue(false);
                        new CompleteDialogFragment().show(getActivity().getSupportFragmentManager(), "");
                    } else {
                        Analytics.getInstance(getActivity()).logError(AnalyticsConstants.ERROR_TRANSACTION_API);
                        showError();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    viewModel.isLoading.postValue(false);
                    t.printStackTrace();
                    showError();
                }
            });
        } catch (JniException e) {
            e.printStackTrace();
            showError();
        }
    }

    private void showError() {
        SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.newInstance(R.string.error_donation, R.string.error_donation_message, v -> getActivity().finish());
        simpleDialogFragment.show(getFragmentManager(), "");
    }

    @OnClick(R.id.button_cancel)
    void onClickCancel() {
        getActivity().finish();
    }

    @OnClick(R.id.button_send)
    void onClickSend() {
        sendTransaction();
    }

    @OnClick(R.id.button_wallet)
    void onClickWallet() {
        WalletChooserDialogFragment walletChooser = WalletChooserDialogFragment.newInstance();
        walletChooser.setOnWalletClickListener(wallet -> pickWallet(wallet.getWalletIndex()));
        walletChooser.show(getFragmentManager(), "");
    }
}
