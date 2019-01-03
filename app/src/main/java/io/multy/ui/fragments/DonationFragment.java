/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
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
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.model.responses.FeeRateResponse;
import io.multy.model.responses.MessageResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.MyFeeAdapter;
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

    private final static String ARG_WALLET_ID = "wallet_id";
    private final static String ARG_DONATION_CODE = "donation_code";
    public static final String TAG_SEND_SUCCESS = DonationFragment.class.getSimpleName();


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
    private Wallet wallet;
    private long maxValue;

    public static DonationFragment newInstance(long walletId, int donationCode) {
        DonationFragment donationFragment = new DonationFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_WALLET_ID, walletId);
        args.putInt(ARG_DONATION_CODE, donationCode);
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

        pickWallet(getArguments().getLong(ARG_WALLET_ID));
        setupInput();
        requestRates();

        Analytics.getInstance(getContext()).logDonationSendLaunch(getArguments() == null ?
                0 : getArguments().getInt(ARG_DONATION_CODE, 0));

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

    private void pickWallet(long walletId) {
        wallet = RealmManager.getAssetsDao().getWalletById(walletId);
        maxValue = wallet.getAvailableBalanceNumeric().longValue();
        textWalletName.setText(wallet.getWalletName());

        long donationSum = (wallet.getAvailableBalanceNumeric().longValue() / 100) * 3;
        if (donationSum < Constants.DONATION_MIN_VALUE / 2) {
            donationSum = Constants.DONATION_MIN_VALUE / 2;
        }
        inputDonation.setText(CryptoFormatUtils.satoshiToBtc(donationSum));
    }

    private void setAdapter(ArrayList<Fee> rates) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new MyFeeAdapter(rates, null, new MyFeeAdapter.OnCustomFeeClickListener() {
            @Override
            public void onClickFee(Fee fee) {
            }

            @Override
            public void onClickCustomFee(long currentValue, long limit) {
                showCustomFeeDialog(currentValue);
                logTransactionFee(5);
            }

            @Override
            public void logTransactionFee(int position) {

            }
        }, MyFeeAdapter.FeeType.BTC));
    }

    public void showCustomFeeDialog(long currentValue) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_custom_fee, null);
        dialogBuilder.setView(dialogView);

        final TextInputEditText input = dialogView.findViewById(R.id.input_custom);
        input.setText(currentValue == -1 ? String.valueOf(20) : String.valueOf(currentValue));
        input.setSelection(input.length());

        final LifecycleObserver lifecycleObserver = new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void onResume() {
                input.postDelayed(() -> showKeyboard(getActivity(), input), 150);
            }
        };
        getLifecycle().addObserver(lifecycleObserver);
        dialogBuilder.setOnDismissListener(dialog -> getLifecycle().removeObserver(lifecycleObserver));

        dialogBuilder.setTitle(R.string.custom_fee);
        dialogBuilder.setPositiveButton(R.string.done, (dialog, whichButton) -> {
            ((MyFeeAdapter) recyclerView.getAdapter()).setCustomFee(Long.valueOf(input.getText().toString()), 0);
//            Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_CUSTOM_SET, viewModel.getChainId());
        });
        dialogBuilder.setNegativeButton(R.string.cancel, (dialog, whichButton) -> {
//            Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_CUSTOM_CANCEL, viewModel.getChainId());
        });
        dialogBuilder.create().show();
    }

    private void requestRates() {
        viewModel.isLoading.postValue(true);
        MultyApi.INSTANCE.getFeeRates(wallet.getCurrencyId(), wallet.getNetworkId()).enqueue(new Callback<FeeRateResponse>() {
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

    private void sendTransaction(String receiverAddress) {
        viewModel.isLoading.setValue(true);
        double btcValue = Double.parseDouble(inputDonation.getText().toString());
        long satoshiValue = (long) (btcValue * Math.pow(10, 8));

        if (satoshiValue < Constants.DONATION_MIN_VALUE / 2) {
            viewModel.errorMessage.setValue("Too low donation amount.");
            return;
        }

        if (satoshiValue > maxValue) {
            Toast.makeText(getActivity(), "Donation amount is bigger than available.", Toast.LENGTH_SHORT).show();
            return;
        }


        final byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();
        final int addressesSize = wallet.getBtcWallet().getAddresses().size();
        final String fee = String.valueOf(((MyFeeAdapter) recyclerView.getAdapter()).getSelectedFee().getAmount());
        final String donationAddress = "";
        final String amount = String.valueOf(CryptoFormatUtils.btcToSatoshi(inputDonation.getText().toString()));

        try {
            final String changeAddress = NativeDataHelper.makeAccountAddress(seed, wallet.getIndex(), addressesSize,
                    wallet.getCurrencyId(),
                    wallet.getNetworkId());
            byte[] transactionHex = NativeDataHelper.makeTransaction(wallet.getId(), wallet.getNetworkId(), seed, wallet.getIndex(), amount,
                    fee, "0", receiverAddress, changeAddress, donationAddress, false);

            MultyApi.INSTANCE.sendHdTransaction(new HdTransactionRequestEntity(wallet.getCurrencyId(), wallet.getNetworkId(),
                    new HdTransactionRequestEntity.Payload(changeAddress, addressesSize, wallet.getIndex(), byteArrayToHex(transactionHex)))).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    if (response.isSuccessful()) {
                        viewModel.isLoading.postValue(false);
                        CompleteDialogFragment completeDialog = new CompleteDialogFragment();
                        if (getArguments() != null) {
                            Bundle donateArgs = new Bundle();
                            donateArgs.putInt(Constants.FEATURE_ID, getArguments().getInt(ARG_DONATION_CODE, 0));
                            completeDialog.setArguments(donateArgs);
                        }
                        completeDialog.show(getActivity().getSupportFragmentManager(), TAG_SEND_SUCCESS);
                    } else {
                        Analytics.getInstance(getActivity()).logError(AnalyticsConstants.ERROR_TRANSACTION_API);
                        showError();
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
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
        SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.newInstanceNegative(R.string.error_donation, R.string.error_donation_message, v -> getActivity().finish());
        simpleDialogFragment.show(getActivity().getSupportFragmentManager(), "");
    }

    @OnClick(R.id.button_cancel)
    void onClickCancel() {
        getActivity().finish();
    }

    @OnClick(R.id.button_send)
    void onClickSend(View view) {
//        new CompleteDialogFragment().show(getActivity().getSupportFragmentManager(), "");
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 1000);
        //if wallet is on testnet then address for donate should be "mnUtMQcs3s8kSkSRXpREVtJamgUCWpcFj4"
        String addressForDonate;
        if (wallet.getNetworkId() == NativeDataHelper.NetworkId.TEST_NET.getValue()) {
            addressForDonate = Constants.DONATION_ADDRESS_TESTNET;
        } else {
            addressForDonate = RealmManager.getSettingsDao().getDonationAddress(getArguments().getInt(ARG_DONATION_CODE, 0));
        }
        if (addressForDonate != null && recyclerView.getAdapter() != null) {
            sendTransaction(addressForDonate);
        }
        Analytics.getInstance(view.getContext()).logDonationSendDonateClick(getArguments() == null ? 0 : getArguments().getInt(ARG_DONATION_CODE, 0));
    }

    @OnClick(R.id.button_wallet)
    void onClickWallet() {
        WalletChooserDialogFragment walletChooser = WalletChooserDialogFragment.newInstance(true);
        walletChooser.setOnWalletClickListener(wallet -> pickWallet(wallet.getId()));
        walletChooser.show(getFragmentManager(), "");
    }
}
