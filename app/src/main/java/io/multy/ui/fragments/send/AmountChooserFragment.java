/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;

import butterknife.BindDimen;
import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.AssetSendViewModel;


public class AmountChooserFragment extends BaseFragment {

    public static AmountChooserFragment newInstance() {
        return new AmountChooserFragment();
    }

    @BindView(R.id.root)
    ConstraintLayout root;
    @BindView(R.id.group_send)
    Group groupSend;
    @BindView(R.id.input_balance_original)
    EditText inputOriginal;
    @BindView(R.id.input_balance_currency)
    EditText inputCurrency;
    @BindView(R.id.text_spendable)
    TextView textSpendable;
    @BindView(R.id.text_total)
    TextView textTotal;
    @BindView(R.id.text_max)
    TextView textMax;
    @BindView(R.id.switcher)
    SwitchCompat switcher;
    @BindView(R.id.container_input_original)
    ConstraintLayout containerInputOriginal;
    @BindView(R.id.container_input_currency)
    ConstraintLayout containerInputCurrency;

    @BindInt(R.integer.zero)
    int zero;
    @BindDimen(R.dimen.amount_size_huge)
    int sizeHuge;
    @BindDimen(R.dimen.amount_size_medium)
    int sizeMedium;
    @BindString(R.string.donation_format_pattern)
    String formatPattern;
    @BindString(R.string.donation_format_pattern_bitcoin)
    String formatPatternBitcoin;

    private boolean isAmountSwapped;

    private AssetSendViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amount_chooser, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        isAmountSwapped = false;
        textSpendable.append(viewModel.getWallet().getBalanceWithCode(CurrencyCode.BTC));
        setupSwitcher();
        setupInputOriginal();
        setupInputCurrency();
        setEmptyTotalWithFee();
        return view;
    }

    @OnClick(R.id.button_next)
    void onClickNext() {
//        if (!TextUtils.isEmpty(inputOriginal.getText())
//                && isParsable(inputOriginal.getText().toString())
//                && Double.valueOf(inputOriginal.getText().toString()) != zero) {
//            if (!TextUtils.isEmpty(inputOriginal.getText()) &&
//                    (Double.parseDouble(inputOriginal.getText().toString())
//                            + viewModel.getFee().getAmount()
//                            + (viewModel.getDonationAmount() == null ? zero : Double.parseDouble(viewModel.getDonationAmount()))
//                            > viewModel.getWallet().getBalance())) {
//                Toast.makeText(getActivity(), R.string.error_balance, Toast.LENGTH_LONG).show();
//            } else {
                viewModel.setAmount(Double.valueOf(inputOriginal.getText().toString()));
                ((AssetSendActivity) getActivity()).setFragment(R.string.ready_to_send, R.id.container, SendSummaryFragment.newInstance());
//            }
//        } else {
//            Toast.makeText(getActivity(), R.string.choose_amount, Toast.LENGTH_SHORT).show();
//        }
    }

    @OnClick(R.id.image_swap)
    void onClickImageSwap() {
        if (isAmountSwapped) {
            inputOriginal.requestFocus();
        } else {
            inputCurrency.requestFocus();
        }
    }

    @OnClick(R.id.text_max)
    void onClickMax() {
        if (textMax.isSelected()) {
            textMax.setSelected(false);
        } else {
            textMax.setSelected(true);
        }
        switcher.setChecked(false);
        inputOriginal.setText(String.valueOf(viewModel.getWallet().getBalance()));
        inputCurrency.setText(new DecimalFormat(formatPattern)
                .format(viewModel.getWallet().getBalance() * viewModel.getExchangePrice().getValue()));
        setTotalAmountWithWallet();
    }

    private void animateOriginalBalance() {
        containerInputOriginal.animate().scaleY(1.5f).setInterpolator(new AccelerateInterpolator()).setDuration(300);
        containerInputOriginal.animate().scaleX(1.5f).setInterpolator(new AccelerateInterpolator()).setDuration(300);
        containerInputCurrency.animate().scaleY(1f).setInterpolator(new AccelerateInterpolator()).setDuration(300);
        containerInputCurrency.animate().scaleX(1f).setInterpolator(new AccelerateInterpolator()).setDuration(300);
        isAmountSwapped = false;
        inputOriginal.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_main));
        inputCurrency.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_grey));
    }

    private void animateCurrencyBalance() {
        containerInputOriginal.animate().scaleY(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        containerInputOriginal.animate().scaleX(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        containerInputCurrency.animate().scaleY(1.5f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        containerInputCurrency.animate().scaleX(1.5f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        isAmountSwapped = true;
        inputOriginal.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_grey));
        inputCurrency.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_main));
    }

    private void setupInputOriginal() {
        inputOriginal.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateOriginalBalance();
                if (!TextUtils.isEmpty(inputOriginal.getText().toString())) {
                    setTotalAmountForInput();
                }
            }
        });

        inputOriginal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isAmountSwapped) { // if currency input is main
                    if (!TextUtils.isEmpty(charSequence)) {
                        if (isParsable(charSequence.toString())) {
                            inputCurrency.setText(new DecimalFormat(formatPattern)
                                    .format(viewModel.getExchangePrice().getValue() * Double.parseDouble(charSequence.toString())));
                            setTotalAmountForInput();
                        }
                    } else {
                        setEmptyTotalWithFee();
                        inputCurrency.getText().clear();
                        inputOriginal.getText().clear();
//                        textTotal.getEditableText().clear();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setupInputCurrency() {
        inputCurrency.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateCurrencyBalance();
                if (!TextUtils.isEmpty(inputCurrency.getText().toString())) {
                    setTotalAmountForInput();
                }
            }
        });

        inputCurrency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (isAmountSwapped) {
                    if (!TextUtils.isEmpty(charSequence)) {
                        if (isParsable(charSequence.toString())) {
                            inputOriginal.setText(new DecimalFormat(formatPatternBitcoin)
                                    .format(Double.parseDouble(charSequence.toString()) / viewModel.getExchangePrice().getValue()));
                            setTotalAmountForInput();
                        }
                    } else {
                        setEmptyTotalWithFee();
                        inputCurrency.getText().clear();
                        inputOriginal.getText().clear();
//                        textTotal.getEditableText().clear();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setupSwitcher() {
        switcher.setOnCheckedChangeListener((compoundButton, isChecked) -> {
//            viewModel.setPayForCommission(isChecked);
            if (isChecked) {
//                if (viewModel.getWalletLive().getBalance() > Double.parseDouble(inputOriginal.getText().toString())) {
//
//                }
            } else {

            }
            setTotalAmountForInput();
        });
    }

    /**
     * Sets total spending amount from wallet.
     * Checks if amount are set in original currency or usd, eur, etc.
     */
    private void setTotalAmountWithWallet() {
        if (isAmountSwapped) {
            textTotal.setText(String.valueOf(viewModel.getWallet().getBalance() * viewModel.getExchangePrice().getValue()));
            textTotal.append(Constants.SPACE);
            textTotal.append(CurrencyCode.USD.name());
        } else {
            textTotal.setText(viewModel.getWallet().getBalanceWithCode(CurrencyCode.BTC));
        }
    }

    private void setTotalAmountForInput() {
        if (isAmountSwapped) {                                  // if currency input is main we set total balance in currency (usd, eur, etc.)
            if (!TextUtils.isEmpty(inputCurrency.getText())) {  // checks input for value to not parse null
                if (switcher.isChecked()) {                     // if pay for commission is checked we add fee and donation to total amount
                    textTotal.setText(new DecimalFormat(formatPatternBitcoin)
                            .format(Double.parseDouble(inputCurrency.getText().toString())
                                    + (viewModel.getFee().getAmount()
                                    + (viewModel.getDonationAmount() == null ? zero : Double.parseDouble(viewModel.getDonationAmount())))
                                    * viewModel.getExchangePrice().getValue()));
                } else {                                        // if pay for commission is unchecked we add just value from input to total amount
                    textTotal.setText(new DecimalFormat(formatPatternBitcoin)
                            .format(Double.parseDouble(inputCurrency.getText().toString())));
                }
                textTotal.append(Constants.SPACE);
                textTotal.append(CurrencyCode.USD.name());
            } else {
                setEmptyTotalWithFee();
            }
        } else {
            if (!TextUtils.isEmpty(inputOriginal.getText())) { // checks input for value to not parse null
                if (switcher.isChecked()) {                    // if pay for commission is checked we add fee and donation to total amount
                    textTotal.setText(new DecimalFormat(formatPatternBitcoin)
                            .format(Double.parseDouble(inputOriginal.getText().toString())
                                    + viewModel.getFee().getAmount()
                                    + (viewModel.getDonationAmount() == null ? zero : Double.parseDouble(viewModel.getDonationAmount()))));
                } else {                                       // if pay for commission is unchecked we add just value from input to total amount
                    textTotal.setText(inputOriginal.getText());
                }
                textTotal.append(Constants.SPACE);
                textTotal.append(CurrencyCode.BTC.name());
            } else {
                setEmptyTotalWithFee();
            }
        }
    }

    private boolean isParsable(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void setEmptyTotalWithFee() {
        if (switcher.isChecked()) {
            if (isAmountSwapped) {
                textTotal.setText(new DecimalFormat(formatPatternBitcoin)
                        .format((viewModel.getFee().getAmount()
                                + (viewModel.getDonationAmount() == null ? zero : Double.parseDouble(viewModel.getDonationAmount())))
                                * viewModel.getExchangePrice().getValue()));
                textTotal.append(Constants.SPACE);
                textTotal.append(CurrencyCode.USD.name());
            } else {
                textTotal.setText(new DecimalFormat(formatPatternBitcoin)
                        .format(viewModel.getFee().getAmount()
                                + (viewModel.getDonationAmount() == null ? zero : Double.parseDouble(viewModel.getDonationAmount()))));
                textTotal.append(Constants.SPACE);
                textTotal.append(CurrencyCode.BTC.name());
            }
        } else {
            textTotal.getEditableText().clear();
        }
    }

}