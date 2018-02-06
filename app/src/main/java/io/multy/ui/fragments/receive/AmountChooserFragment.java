/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.receive;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.BaseActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.NumberFormatter;
import io.multy.viewmodels.AssetRequestViewModel;


public class AmountChooserFragment extends BaseFragment {

    public static AmountChooserFragment newInstance() {
        return new AmountChooserFragment();
    }

    @BindView(R.id.group_send)
    Group groupSend;
    @BindView(R.id.input_balance_original)
    EditText inputOriginal;
    @BindView(R.id.input_balance_currency)
    EditText inputCurrency;
    @BindView(R.id.button_next)
    TextView buttonNext;
    @BindView(R.id.container_input_original)
    ConstraintLayout containerInputOriginal;
    @BindView(R.id.container_input_currency)
    ConstraintLayout containerInputCurrency;

    @BindInt(R.integer.zero)
    int zero;
    @BindInt(R.integer.one)
    int one;
    @BindString(R.string.point)
    String point;
    @BindString(R.string.donation_format_pattern)
    String formatPattern;
    @BindString(R.string.donation_format_pattern_bitcoin)
    String formatPatternBitcoin;

    private boolean isAmountSwapped;
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
        View view = inflater.inflate(R.layout.fragment_amount_chooser, container, false);
        ButterKnife.bind(this, view);

        if (viewModel.getAmount() != zero) {
            inputOriginal.setText(NumberFormatter.getInstance().format(viewModel.getAmount()));
            inputCurrency.setText(NumberFormatter.getFiatInstance()
                    .format((viewModel.getExchangePrice() * viewModel.getAmount())));
        }
        groupSend.setVisibility(View.GONE);
        buttonNext.setGravity(Gravity.CENTER);
        buttonNext.setText(R.string.done);
        setupInputOriginal();
        setupInputCurrency();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        inputOriginal.requestFocus();
        inputOriginal.postDelayed(() -> showKeyboard(getActivity(), inputOriginal),300);
    }

    @OnClick(R.id.image_swap)
    void onClickImageSwap() {
        if (isAmountSwapped) {
            inputOriginal.requestFocus();
        } else {
            inputCurrency.requestFocus();
        }
    }

    @OnClick(R.id.button_next)
    void onClickNext() {
        if (!TextUtils.isEmpty(inputOriginal.getText())) {
            viewModel.setAmount(Double.valueOf(inputOriginal.getText().toString()));
        }
        getActivity().onBackPressed();
    }

    private void setupInputOriginal() {
        inputOriginal.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateOriginalBalance();
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
                            inputCurrency.setText(NumberFormatter.getFiatInstance()
                                    .format((viewModel.getExchangePrice() * Double.parseDouble(charSequence.toString()))));
                        }
                    } else {
                        inputCurrency.getText().clear();
                        inputOriginal.getText().clear();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkForPointAndZeros(editable.toString(), inputOriginal);
                checkMaxLengthBeforePoint(inputOriginal, 6);
            }
        });
    }

    private void setupInputCurrency() {
        inputCurrency.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateCurrencyBalance();
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
                            inputOriginal.setText(NumberFormatter.getInstance()
                                    .format(Double.parseDouble(charSequence.toString())
                                            / viewModel.getExchangePrice()));
                        }
                    } else {
                        inputCurrency.getText().clear();
                        inputOriginal.getText().clear();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkForPointAndZeros(editable.toString(), inputCurrency);
                checkMaxLengthBeforePoint(inputCurrency, 9);
                if (!TextUtils.isEmpty(editable)
                        && editable.toString().contains(point)
                        && editable.length() - editable.toString().indexOf(point) >= 4) {
                    inputCurrency.setText(editable.toString().substring(0, editable.toString().indexOf(point) + 3));
                    inputCurrency.setSelection(inputCurrency.getText().length());
                }
            }
        });
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

    private boolean isParsable(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void checkForPointAndZeros(String input, EditText inputView) {
        if (!TextUtils.isEmpty(input)
                && input.length() == one
                && input.contains(point)) {
            String result = input.replaceAll(point, "");
            inputView.setText(result);
        } else if (input.length() == getResources().getInteger(R.integer.two)
                && input.equals("00")) {
            inputView.setText("0");
            inputView.setSelection(1);
        }
    }

    private void checkMaxLengthBeforePoint(EditText input, int max) {
        int selection = input.getSelectionStart();
        String amount = input.getText().toString();
        if (!TextUtils.isEmpty(amount) && amount.length() > max) {
            if (amount.contains(point)) {
                if (amount.indexOf(point) > max) {
                    input.setText(amount.substring(amount.indexOf(point) - max, amount.length()));
                    if (selection > input.getText().length()) {
                        input.setSelection(input.getText().length());
                    } else {
                        input.setSelection(selection);
                    }
                }
            } else {
                input.setText(amount.substring(0, max));
                if (selection > input.getText().length()) {
                    input.setSelection(input.getText().length());
                } else {
                    input.setSelection(selection);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        ((BaseActivity)getActivity()).hideKeyboard(getActivity());
        super.onDestroy();
    }
}
