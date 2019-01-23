/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send.ethereum;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.samwolfand.oneprefs.Prefs;

import java.math.BigDecimal;
import java.math.BigInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.core.Account;
import io.multy.model.core.Builder;
import io.multy.model.core.Payload;
import io.multy.model.core.Transaction;
import io.multy.model.core.TransactionBuilder;
import io.multy.model.core.TransactionResponse;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.EthWallet;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.activities.BaseActivity;
import io.multy.ui.activities.TokenSendActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.util.NumberFormatter;
import io.multy.util.analytics.Analytics;
import io.multy.viewmodels.AssetSendViewModel;


public class TokenAmountChooserFragment extends BaseFragment {

    public static final String TAG = TokenAmountChooserFragment.class.getSimpleName();

    public static TokenAmountChooserFragment newInstance() {
        return new TokenAmountChooserFragment();
    }

    @BindView(R.id.group_send)
    Group groupSend;
    @BindView(R.id.input_balance_original)
    EditText inputOriginal;
    @BindView(R.id.text_spendable)
    TextView textSpendable;
    @BindView(R.id.text_total)
    TextView textTotal;
    @BindView(R.id.text_max)
    TextView textMax;
    @BindView(R.id.container_input_original)
    ConstraintLayout containerInputOriginal;
    @BindView(R.id.button_clear_original)
    View buttonClearOriginal;
    @BindView(R.id.text_currency_code_original)
    TextView textCurrencyCode;
    @BindView (R.id.button_next)
     TextView buttonNext;



    private AssetSendViewModel viewModel;
    private CurrenciesRate currenciesRate;
    private boolean isAmountSwapped = false;
    private BigDecimal spendableWei;
    private double transactionPriceEth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        View view = inflater.inflate(R.layout.fragment_amount_chooser_token, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        setBaseViewModel(viewModel);
        buttonNext.setEnabled(false);
        currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();

        if (viewModel.getWallet() == null || !viewModel.getWallet().isValid()) {
            viewModel.setWallet(RealmManager.getAssetsDao()
                    .getWalletById(getActivity().getIntent().getExtras().getLong(Constants.EXTRA_WALLET_ID, -1)));
        }

        subscribeToUpdates();
        initTransactionPrice();
        textCurrencyCode.setText(viewModel.tokenCode.getValue());
        textSpendable.setText(String.format("%s %s", viewModel.tokenBalance.getValue(), viewModel.tokenCode.getValue()));


        if (!viewModel.isAmountScanned() && viewModel.getAmount() == 0) {
            Analytics.getInstance(getActivity()).logSendChooseAmountLaunch(viewModel.getChainId());
        } else {
            inputOriginal.setText(viewModel.getAmount() > 0 ? String.valueOf(viewModel.getAmount()) : "");
        }


        inputOriginal.setOnFocusChangeListener((view1, hasFocus) -> {
            if (hasFocus) {
                inputOriginal.setSelection(inputOriginal.getText().length());

            }
        });


        inputOriginal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //TODO implement 0 entity check!

                if (!checkMaxLengthAfterPoint(inputOriginal, 8, i, i2)) {
                    return;
                }
                checkMaxLengthBeforePoint(inputOriginal, 6, i, i1, i2);

                if (!charSequence.toString().isEmpty() && isParsable(charSequence.toString())){
                    setTotalAmountForInput();
                } else {
                    textTotal.setText("");
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkForPointAndZeros(s.toString(), inputOriginal);
            }
        });
        return view;
    }

    private void initTransactionPrice() {
        if (viewModel.getWallet().getMultisigWallet() != null) {
            if (viewModel.estimation.getValue() == null) {
                viewModel.requestMultisigEstimates(viewModel.getWallet().getActiveAddress().getAddress());
            } else {
                transactionPriceEth = EthWallet.getTransactionMultisigPrice(viewModel.getFee().getAmount(), Long.parseLong(viewModel.estimation.getValue().getSubmitTransaction()));
            }
        } else {
            transactionPriceEth = EthWallet.getTransactionPrice(viewModel.getFee().getAmount());
            viewModel.gasLimit.observe(this, customGasLimit -> {
                if (!TextUtils.isEmpty(customGasLimit)) {
                    transactionPriceEth = EthWallet.getTransactionPrice(viewModel.getFee().getAmount(), Long.parseLong(customGasLimit));
                }
            });
        }

    }

    @Override
    public void onDestroy() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).setOnLockCLoseListener(null);
        }
        super.onDestroy();
    }

    @OnClick(R.id.button_next)
    public void onClickNext() {
        String inputAmount = inputOriginal.getText().toString();
        if (!inputAmount.isEmpty() && isParsable(inputAmount) && Double.parseDouble(inputAmount)> 0) {

            Wallet wallet = viewModel.getWallet();
            BigDecimal amount = new BigDecimal(inputOriginal.getText().toString()).multiply(new BigDecimal(Math.pow(10, viewModel.decimals.getValue())));
            BigDecimal balance = new BigDecimal(viewModel.tokenBalance.getValue()).multiply(new BigDecimal(Math.pow(10, viewModel.decimals.getValue())));
            viewModel.tokensAmount.setValue(inputOriginal.getText().toString());

            Payload payload = new Payload(wallet.getActiveAddress().getAmountString(),
                    viewModel.contractAddress.getValue(),
                    balance.toBigInteger().toString(),
                    amount.toBigInteger().toString(),
                    viewModel.getReceiverAddress().getValue());

            Builder builder = new Builder(Builder.TYPE_ERC20, Builder.ACTION_TRANSFER, payload);

            Transaction transaction = new Transaction(wallet.getEthWallet().getNonce(), new io.multy.model.core.Fee(
                    String.valueOf(viewModel.getFee().getAmount()), Constants.GAS_LIMIT_TOKEN_TANSFER));


            //TODO need to move this dummy check to Transaction Helper class to prevent such stupid check

            final int walletIndex = Prefs.getBoolean(Constants.PREF_METAMASK_MODE, false) ? wallet.getActiveAddress().getIndex() : wallet.getIndex();
            final int addressIndex = Prefs.getBoolean(Constants.PREF_METAMASK_MODE, false) ? wallet.getIndex() : wallet.getActiveAddress().getIndex();



            TransactionBuilder transactionBuilder = new TransactionBuilder(
                    NativeDataHelper.Blockchain.ETH.getName(),
                    NativeDataHelper.NetworkId.ETH_MAIN_NET.getValue(),
                    Account.getAccount(walletIndex, addressIndex, wallet.getNetworkId()),
                    builder,
                    transaction);

            try {
                String json = NativeDataHelper.makeTransactionJSONAPI(new Gson().toJson(transactionBuilder));
                TransactionResponse transactionResponse = new Gson().fromJson(json, TransactionResponse.class);
                if (transactionResponse == null) {
                    viewModel.errorMessage.setValue("Error building transaction");
                } else {
                    viewModel.transaction.setValue(transactionResponse.getTransactionHex());
                }
            } catch (JniException e) {
                viewModel.errorMessage.setValue(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets total spending amount from wallet.
     * Checks if amount are set in original currency or usd, eur, etc.
     */
    private void setMaxAmountToSpend() {
        if (isAmountSwapped) {
            textTotal.setText(viewModel.getWallet().getFiatBalanceLabelTrimmed());
            textTotal.append(Constants.SPACE);
            textTotal.append(CurrencyCode.USD.name());
        } else {
            textTotal.setText(viewModel.getWallet().getBalanceLabel());
        }
    }

    private void subscribeToUpdates() {
        viewModel.transaction.observe(this, s -> ((TokenSendActivity) getActivity()).setFragment(R.string.send_summary, R.id.container, TokenSendSummaryFragment.newInstance()));
    }

    @OnClick(R.id.button_clear_original)
    void onClickClearOriginal() {
        inputOriginal.setText("");
//        switcher.setChecked(true);
    }

    @OnClick(R.id.container_input_original)
    void onClickInputOriginal() {
        if (!inputOriginal.hasFocus()) {
            inputOriginal.requestFocus();
        }
        showKeyboard(getActivity(), inputOriginal);
    }

    @OnClick(R.id.text_max)
    void onClickMax() {
        inputOriginal.setText(viewModel.tokenBalance.getValue());
    }


    private void checkForPointAndZeros(String input, EditText inputView) {
        int selection = inputView.getSelectionStart();
        if (!TextUtils.isEmpty(input) && input.length() == 1 && input.contains(".")) {
            String result = input.replaceAll(".", "0.");
            inputView.setText(result);
            inputView.setSelection(inputView.getText().toString().trim().length());
        } else if (!TextUtils.isEmpty(input) && input.startsWith("0") &&
                !input.startsWith("0.") && input.length() > 1) {
            inputView.setText(input.substring(1, input.length()));
            inputView.setSelection(selection - 1);
        }
    }

    private void checkMaxLengthBeforePoint(EditText input, int max, int start, int end, int count) {
        String amount = input.getText().toString();
        if (!TextUtils.isEmpty(amount) && amount.length() > max) {
            if (amount.contains(".")) {
                if (amount.indexOf(".") > max) {
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

    private boolean checkMaxLengthAfterPoint(EditText input, int max, int start, int count) {
        String amount = input.getText().toString();
        if (!TextUtils.isEmpty(amount) && amount.contains(".")) {
            if (amount.length() - (amount.indexOf(".") + 1) > max) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(amount.substring(0, start));
                stringBuilder.append(amount.substring(start + count, amount.length()));
                input.setText(stringBuilder.toString());
                input.setSelection(start);
                return false;
            }
        }
        return true;
    }

    private boolean isParsable(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private void setTotalAmountForInput() {
        try {
                if (!TextUtils.isEmpty(inputOriginal.getText())) { // checks input for value to not parse null

                    double currentAmount = Double.parseDouble(inputOriginal.getText().toString());
                    double maxValue = Double.parseDouble(viewModel.tokenBalance.getValue());

                    if (currentAmount > 0){
                        if (maxValue >= currentAmount){
                            buttonNext.setEnabled(true);
                            buttonNext.setEnabled(true);
                            textTotal.setText(String.format("%s %s", inputOriginal.getText().toString(), viewModel.tokenCode.getValue()));
                        } else {
                            buttonNext.setEnabled(true);
                            int substringCount = inputOriginal.getText().length() - 1;
                            textTotal.setText(inputOriginal.getText().subSequence(0, substringCount < 0 ? 0 : substringCount));
                            inputOriginal.setText(inputOriginal.getText().subSequence(0, substringCount < 0 ? 0 : substringCount));
                            inputOriginal.setSelection(inputOriginal.getText().length());
                            viewModel.errorMessage.setValue(getString(R.string.enter_sum_too_much));
                            return;
                        }
                    } else {
                        buttonNext.setEnabled(false);
                    }

                } else {
                    textTotal.setText("");
                    buttonNext.setEnabled(false);
                }
//            }
        } catch (Throwable t) {
            t.printStackTrace();
            int substringCount = inputOriginal.getText().length() - 1;
            buttonNext.setEnabled(true);
            textTotal.setText(inputOriginal.getText().subSequence(0, substringCount < 0 ? 0 : substringCount));
            inputOriginal.setText(inputOriginal.getText().subSequence(0, substringCount < 0 ? 0 : substringCount));
            inputOriginal.setSelection(inputOriginal.getText().length());
        }
    }

}