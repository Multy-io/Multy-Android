/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.receive;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.BaseActivity;
import io.multy.ui.activities.FastReceiveActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.NumberFormatter;
import io.multy.viewmodels.AssetRequestViewModel;

import static android.app.Activity.RESULT_OK;


public class AmountChooserFragment extends BaseFragment implements BaseActivity.OnLockCloseListener {

    public static AmountChooserFragment newInstance() {
        return new AmountChooserFragment();
    }

    @BindView(R.id.group_send)
    Group groupSend;
    @BindView(R.id.input_balance_original)
    EditText inputOriginal;
    @BindView(R.id.text_currency_code_original)
    TextView textCurrencyOriginal;
    @BindView(R.id.input_balance_currency)
    EditText inputCurrency;
    @BindView(R.id.button_next)
    TextView buttonNext;
    @BindView(R.id.button_clear_original)
    View buttonClearOriginal;
    @BindView(R.id.button_clear_currency)
    View buttonClearCurrency;
    @BindView(R.id.container_input_original)
    ConstraintLayout containerInputOriginal;
    @BindView(R.id.container_input_currency)
    ConstraintLayout containerInputCurrency;

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
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).setOnLockCLoseListener(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        View view = inflater.inflate(R.layout.fragment_amount_chooser, container, false);
        ButterKnife.bind(this, view);

        setupInputOriginal();
        setupInputCurrency();
        if (viewModel.getAmount() != 0) {
            inputOriginal.setText(NumberFormatter.getInstance().format(viewModel.getAmount()));
            inputCurrency.setText(NumberFormatter.getFiatInstance()
                    .format((viewModel.getExchangePrice() * viewModel.getAmount())));
        }
        textCurrencyOriginal.setText(viewModel.getWallet().getCurrencyName());
        groupSend.setVisibility(View.GONE);
        buttonNext.setGravity(Gravity.CENTER);
        buttonNext.setText(R.string.done);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!(getActivity() instanceof BaseActivity && ((BaseActivity) getActivity()).isLockVisible())) {
            requestFocusForInput();
        }
    }

    @Override
    public void onDestroyView() {
        hideKeyboard(getActivity());
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).setOnLockCLoseListener(null);
        }
        super.onDestroy();
    }

    @Override
    public void onLockClosed() {
        requestFocusForInput();
    }

    private void setupInputOriginal() {
        inputOriginal.setOnTouchListener((v, event) -> {
            inputOriginal.setSelection(inputOriginal.getText().length());
            if (!inputOriginal.hasFocus()) {
                inputOriginal.requestFocus();
                return true;
            }
            showKeyboard(getActivity(), v);
            return true;
        });

        inputOriginal.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateOriginalBalance();
                inputOriginal.setSelection(inputOriginal.getText().length());
                buttonClearOriginal.setVisibility(View.VISIBLE);
                buttonClearCurrency.setVisibility(View.GONE);
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
                checkMaxLengthAfterPoint(inputOriginal, 9, i, i2);
                checkMaxLengthBeforePoint(inputOriginal, 6, i, i1, i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkForPointAndZeros(editable.toString(), inputOriginal);
            }
        });
    }

    private void setupInputCurrency() {
        inputCurrency.setOnTouchListener((v, event) -> {
            inputCurrency.setSelection(inputCurrency.getText().length());
            if (!inputCurrency.hasFocus()) {
                inputCurrency.requestFocus();
                return true;
            }
            showKeyboard(getActivity(), v);
            return true;
        });
        inputCurrency.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateCurrencyBalance();
                inputCurrency.setSelection(inputCurrency.getText().length());
                buttonClearOriginal.setVisibility(View.GONE);
                buttonClearCurrency.setVisibility(View.VISIBLE);
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
                                    .format(Double.parseDouble(charSequence.toString()) / viewModel.getExchangePrice()));
                        }
                    } else {
                        inputCurrency.getText().clear();
                        inputOriginal.getText().clear();
                    }
                }
                checkMaxLengthAfterPoint(inputCurrency, 3, i, i2);
                if (isAmountSwapped) {
                    checkMaxLengthBeforePoint(inputCurrency, 9, i, i1, i2);
                } else {
                    checkMaxLengthBeforePoint(inputCurrency, 10, i, i1, i2);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkForPointAndZeros(editable.toString(), inputCurrency);
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
        int selection = inputView.getSelectionStart();
        if (!TextUtils.isEmpty(input) && input.length() == 1 && input.contains(".")) {
            String result = input.replaceAll(".", "0.");
            inputView.setText(result);
            inputView.setSelection(result.length());
        } else if (!TextUtils.isEmpty(input) && input.startsWith("0") &&
                !input.startsWith("0.") && input.length() > 1) {
            inputView.setText(input.substring(1, input.length()));
            inputView.setSelection(selection - 1);
        }
    }

    private void checkMaxLengthBeforePoint(EditText input, int max, int start, int end, int count) {
        String amount = input.getText().toString();
        if (!TextUtils.isEmpty(amount) && amount.length() > max) {
            if (amount.contains(point)) {
                if (amount.indexOf(point) > max) {
                    if (start != 0 && end != amount.length() && count == amount.length()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(amount.substring(0, start));
                        stringBuilder.append(amount.substring(start + count, amount.length()));
                        input.setText(stringBuilder.toString());
                        if (start <= input.getText().length()) {
                            input.setSelection(start);
                        } else {
                            input.setSelection(input.getText().length());
                        }
                    } else {
                        input.setText(amount.substring(0, amount.length() - 1));
                        input.setSelection(input.getText().length());
                    }
                }
            } else {
                if (start != 0 && end != amount.length() && count == amount.length()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(amount.substring(0, start));
                    stringBuilder.append(amount.substring(start + count, amount.length()));
                    input.setText(stringBuilder.toString());
                    input.setSelection(start);
                } else {
                    input.setText(amount.substring(0, amount.length() - 1));
                    input.setSelection(input.getText().length());
                }
            }
        }
    }

    private void checkMaxLengthAfterPoint(EditText input, int max, int start, int count) {
        String amount = input.getText().toString();
        if (!TextUtils.isEmpty(amount) && amount.contains(point)) {
            if (amount.length() - amount.indexOf(point) > max) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(amount.substring(0, start));
                stringBuilder.append(amount.substring(start + count, amount.length()));
                input.setText(stringBuilder.toString());
                input.setSelection(start);
            }
        }
    }

    private void requestFocusForInput() {
        inputOriginal.requestFocus();
        inputOriginal.postDelayed(() -> showKeyboard(getActivity(), inputOriginal), 300);
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
        double amount = 0;
        if (!TextUtils.isEmpty(inputOriginal.getText())) {
            amount = Double.valueOf(inputOriginal.getText().toString());
        }
        if (getActivity() != null && getActivity() instanceof FastReceiveActivity) {
            viewModel.setAmount(amount);
            getActivity().onBackPressed();
        } else {
            if (getActivity() != null && getTargetFragment() != null) {
                Intent data = new Intent().putExtra(Constants.EXTRA_AMOUNT, amount);
                getTargetFragment().onActivityResult(RequestSummaryFragment.AMOUNT_CHOOSE_REQUEST, RESULT_OK, data);
                getActivity().onBackPressed();
            }
        }
    }

    @OnClick(R.id.button_clear_original)
    void onClickClearOriginal() {
        inputOriginal.setText("");
    }

    @OnClick(R.id.button_clear_currency)
    void onClickClearCurrency() {
        inputCurrency.setText("");
    }

    @OnClick(R.id.container_input_original)
    void onClickInputOriginal() {
        if (!inputOriginal.hasFocus()) {
            inputOriginal.requestFocus();
        }
        showKeyboard(getActivity(), inputOriginal);
    }

    @OnClick(R.id.container_input_currency)
    void onClickInputCurrency() {
        if (!inputCurrency.hasFocus()) {
            inputCurrency.requestFocus();
        }
        showKeyboard(getActivity(), inputCurrency);
    }
}
