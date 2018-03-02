/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
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

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.Fee;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.adapters.MyFeeAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetSendViewModel;

public class TransactionFeeFragment extends BaseFragment implements MyFeeAdapter.OnCustomFeeClickListener {

    public static TransactionFeeFragment newInstance() {
        return new TransactionFeeFragment();
    }

    @BindView(R.id.scrollview)
    ScrollView scrollView;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.switcher)
    SwitchCompat switcher;
    @BindView(R.id.text_donation_allow)
    TextView textDonationAllow;
    @BindView(R.id.text_donation_summ)
    TextView textDonationsSum;
    @BindView(R.id.input_donation)
    EditText inputDonation;
    @BindView(R.id.text_fee_currency)
    TextView textFeeCurrency;
    @BindView(R.id.group_donation)
    Group groupDonation;
    @BindString(R.string.donation_format_pattern)
    String formatPattern;

    private AssetSendViewModel viewModel;
    private boolean isDonationChanged;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_fee, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        setBaseViewModel(viewModel);
        inputDonation.setOnFocusChangeListener((view1, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN), 500);
            } else {
                if (!TextUtils.isEmpty(inputDonation.getText())
                        && !inputDonation.getText().toString().equals(getString(R.string.donation_default))) {
                    Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_DONATION_CHANGED, viewModel.getChainId());
                }
            }
        });
        setupSwitcher();
        setupInput();

        viewModel.speeds.observe(this, speeds -> setAdapter());
        viewModel.requestFeeRates(NativeDataHelper.Currency.BTC.getValue());
        Analytics.getInstance(getActivity()).logTransactionFeeLaunch(viewModel.getChainId());
        isDonationChanged = false;
        return view;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new MyFeeAdapter(viewModel.speeds.getValue().asList(), this));
    }

    @Override
    public void onDestroyView() {
        hideKeyboard(getActivity());
        super.onDestroyView();
    }

    private void setupSwitcher() {
        switcher.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                textDonationAllow.setBackground(getResources().getDrawable(R.drawable.shape_top_round_white, null));
                groupDonation.setVisibility(View.VISIBLE);
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_DONATION_ENABLE, viewModel.getChainId());
            } else {
                textDonationAllow.setBackground(getResources().getDrawable(R.drawable.shape_squircle_white, null));
                groupDonation.setVisibility(View.GONE);
                hideKeyboard(getActivity());
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_DONATION_DISABLE, viewModel.getChainId());
            }
        });
    }

    private void setupInput() {
        if (viewModel.getCurrenciesRate() != null) {
            textFeeCurrency.setText(new DecimalFormat(formatPattern).format(Double.parseDouble(inputDonation.getText().toString()) * viewModel.getCurrenciesRate().getBtcToUsd()));
        }
        textFeeCurrency.append(Constants.SPACE);
        textFeeCurrency.append(CurrencyCode.USD.name());

        inputDonation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence)) {
                    textFeeCurrency.setText(new DecimalFormat(formatPattern)
                            .format(Double.parseDouble(charSequence.toString()) * viewModel.getCurrenciesRate().getBtcToUsd()));
                    textFeeCurrency.append(Constants.SPACE);
                    textFeeCurrency.append(CurrencyCode.USD.name());
                } else {
                    textFeeCurrency.setText(Constants.SPACE);
                }
                isDonationChanged = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
            Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_CUSTOM_SET, viewModel.getChainId());
        });
        dialogBuilder.setNegativeButton(R.string.cancel, (dialog, whichButton) -> {
            Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_CUSTOM_CANCEL, viewModel.getChainId());
        });
        dialogBuilder.create().show();
    }

    @Override
    public void onClickCustomFee(long currentValue) {
        showCustomFeeDialog(currentValue);
        logTransactionFee(5);
    }

    @Override
    public void logTransactionFee(int position) {
        switch (position) {
            case 0:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_VERY_FAST, viewModel.getChainId());
                break;
            case 1:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_FAST, viewModel.getChainId());
                break;
            case 2:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_MEDIUM, viewModel.getChainId());
                break;
            case 3:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_SLOW, viewModel.getChainId());
                break;
            case 4:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_VERY_SLOW, viewModel.getChainId());
                break;
            case 5:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_CUSTOM, viewModel.getChainId());
                break;
        }
    }

    @OnClick(R.id.input_donation)
    void onDonationClick() {
        scrollView.postDelayed(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN), 500);
    }

    @OnClick(R.id.button_next)
    void onClickNext() {
        Fee selectedFee = ((MyFeeAdapter) recyclerView.getAdapter()).getSelectedFee();

        if (selectedFee != null) {
            if (selectedFee.getAmount() < 2) {
                selectedFee.setAmount(2);
            }
            viewModel.setFee(selectedFee);

            if (switcher.isChecked()) {
                viewModel.setDonationAmount(inputDonation.getText().toString());
            } else {
                viewModel.setDonationAmount(null);
            }

            ((AssetSendActivity) getActivity()).setFragment(R.string.send_amount, R.id.container, AmountChooserFragment.newInstance());

            if (viewModel.isAmountScanned()) {
                ((AssetSendActivity) getActivity()).setFragment(R.string.send_summary, R.id.container, SendSummaryFragment.newInstance());
            }
        } else {
            Toast.makeText(getActivity(), R.string.choose_transaction_speed, Toast.LENGTH_SHORT).show();
        }

        if (!isDonationChanged) {
            Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_DONATION, viewModel.getChainId());
        }
    }
}
