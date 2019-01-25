/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.exchange;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.multy.R;
import io.multy.model.entities.wallet.RecentAddress;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.activities.MagicSendActivity;
import io.multy.ui.activities.TokenSendActivity;
import io.multy.ui.adapters.RecentAddressesAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.AddressActionsDialogFragment;
import io.multy.ui.fragments.main.contacts.ContactsFragment;
import io.multy.ui.fragments.send.AmountChooserFragment;
import io.multy.ui.fragments.send.TransactionFeeFragment;
import io.multy.ui.fragments.send.ethereum.EthTransactionFeeFragment;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.multy.util.NumberFormatter;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetSendViewModel;
import io.multy.viewmodels.ExchangeViewModel;
import io.realm.RealmResults;

//public class ExchangeFragment extends BaseFragment implements TextWatcher{
public class ExchangeFragment extends BaseFragment {

    public static final int REQUEST_CONTACT = 1101;

    @BindView(R.id.container)
    ConstraintLayout globalLayout;
    @BindView(R.id.tv_exchange_summery_from_wallet)
    TextView tvPayFromWallet;
    @BindView(R.id.ib_exchange_from_icon)
    ImageButton ibPayFromWalletLogo;
    @BindView(R.id.til_exchange_from_crypto)
    TextInputLayout inputFromCryptoLayout;
    @BindView(R.id.tie_exchange_from_crypto)
    TextInputEditText inputFromCrypto;
    @BindView(R.id.til_exchange_from_fiat)
    TextInputLayout inputFromFiatLayout;
    @BindView(R.id.tie_exchange_from_fiat)
    TextInputEditText inputFromFiat;
    @BindView(R.id.tv_exchange_summery_crypto_amount)
    TextView tvSummeryCrypto;
    @BindView(R.id.iv_summery_from_logo)
    ImageView ivSummeryFromLogo;
    @BindView(R.id.tv_exchange_summery_fiat_amount)
    TextView tvSummeryFromFiat;
    @BindView(R.id.button_max)
    TextView buttonMax;
    @BindView(R.id.tv_exchange_rate)
    TextView tvExchangeRate;
    @BindView(R.id.tv_exchange_min_rate)
    TextView tvExchangeMin;
    @BindView(R.id.tv_exchange_select_asset)
    TextView tvExchangeTap;
    @BindView(R.id.til_exchange_to_crtypo)
    TextInputLayout tilExchangeToCryptoLayout;
    @BindView(R.id.tie_exchange_to_crypto)
    TextInputEditText inputToCrypto;
    @BindView(R.id.til_exchange_to_fiat)
    TextInputLayout inputToFiatLayout;
    @BindView(R.id.tv_exchange_summery_receive_wallet)
    TextView tvSummeryToWallet;
    @BindView(R.id.tv_exchange_summery_receive_crypto_amount)
    TextView tvSummeryReceiveCrypto;
    @BindView(R.id.tv_exchange_summery_receive_fiat_amount)
    TextView tvSummeryReceiveFiat;
    @BindView(R.id.iv_summery_to_logo)
    ImageView ivSummeryToLogo;
    @BindView(R.id.tv_exchange_summery_receive)
    TextView tvSummeryToTitle;

    @BindView(R.id.constraint_summery)
    ConstraintLayout summeryLayout;


    @BindView(R.id.ib_receive)
    ImageButton ibReceiveToLogo;

    @OnClick(R.id.ib_receive)
    void onClickReceiveLogo(){
        Log.d("EXCHANGE FR", "LOGO TAPPED");
        viewModel.changeFragment(1);
    }

//    @BindView(R.id.input_address)
//    EditText inputAddress;
//    @BindView(R.id.button_next)
//    TextView buttonNext;
//    @BindView(R.id.recycler_view)
//    RecyclerView recyclerView;

    private ExchangeViewModel viewModel;
    private Unbinder unbinder;
    private RecentAddressesAdapter adapter;

    public static ExchangeFragment newInstance() {
        return new ExchangeFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exchange, container, false);
        this.unbinder = ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(requireActivity()).get(ExchangeViewModel.class);
        setBaseViewModel(viewModel);


        setupGlobalLayout();
        setupPayFromWallet();
        setupInputFromCrypto();
        setupInputToCrypto();
        setupInputFromFiat();
        setupTotalAmount();
//        viewModel.getReceiverAddress().observe(this, s -> {
//            inputAddress.setText(s);
//            inputAddress.setSelection(inputAddress.length());
//        });
//        setupInputAddress();
//        initRecycler();
//        initRecentAddresses();
//        logLaunch();
        return view;
    }

    @Override
    public void onResume() {
        //TODO check this shitcode
//        if (!viewModel.getWallet().isValid()) {
//            viewModel.setWallet(RealmManager.getAssetsDao().getWalletById(getActivity().getIntent()
//                    .getLongExtra(Constants.EXTRA_WALLET_ID, -1)));
//        }
//        if (adapter != null && !adapter.isValidData()) {
//            initRecentAddresses();
//        }
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        hideKeyboard(getActivity());
        unbinder.unbind();
        super.onDestroyView();
    }


    private void setupGlobalLayout(){
        globalLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (globalLayout!=null) {
                    Rect r = new Rect();
                    globalLayout.getWindowVisibleDisplayFrame(r);
                    int screenHeight = globalLayout.getRootView().getHeight();

                    // r.bottom is the position above soft keypad or device button.
                    // if keypad is shown, the r.bottom is smaller than that before.
                    int keypadHeight = screenHeight - r.bottom;

                    if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                        hideTotalSummery();
                    } else {
                        showTotalSummery();
                    }
                }
            }
        });
    }

    private void setupTotalAmount(){
        viewModel.getEstimateTransaction().observe(this, estimation ->{
            if (estimation != null) {
                String from = null;
                if (viewModel.getSendERC20Token().getValue()!= null){
                    from = viewModel.getSendERC20Token().getValue().getName();
                    tvSummeryFromFiat.setVisibility(View.INVISIBLE);
                    tvSummeryCrypto.setText(String.format("%s %s", estimation.getFromCryptoValue(), from));

                } else {
                    from = viewModel.getPayFromWallet().getValue().getCurrencyName();
                    tvSummeryFromFiat.setText(String.format("$ %s", estimation.getFromFiatValue()));
                }
                tvSummeryCrypto.setText(String.format("%s %s", estimation.getFromCryptoValue(), from));
                tvSummeryReceiveCrypto.setText(String.format("%s %s", inputToCrypto.getText().toString(), viewModel.getSelectedAsset().getValue().getName()));
            } else {
                setSummeryToZero();
            }
        });
    }

    private void setupInputFromFiat(){
        setupOnFocusListnere(inputFromFiat);


        inputFromFiat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {
                if (inputFromFiat != null) {
                    if (getActivity().getCurrentFocus() == inputFromFiat) {
                        if (!text.toString().isEmpty()) {
                            if (isParsable(text.toString())) {
                                checkForPointAndZeros(text.toString(), inputFromFiat);
                                double raw = Float.parseFloat(text.toString());
                                double currRate = viewModel.getCurrenciesRate();

                                //eth rarte 100 input is 1000, setup 10 Eth
                                //          raw / currRate
                                ///

                                double fromCryptoValue = raw / currRate;
                                double receiveCryptoValue = fromCryptoValue * viewModel.getExchangeRate().getValue();

                                inputToCrypto.setText(NumberFormatter.getInstance().format(receiveCryptoValue));
                                inputFromCrypto.setText(NumberFormatter.getInstance().format(fromCryptoValue));
                                checkMaxLengthBeforePoint(inputFromFiat, 10, i, i1, i2);
                                checkForCanSpend(inputFromFiat, fromCryptoValue);
                            }
                        } else {
//                        inputFromFiat.setText("0");
                            inputToCrypto.setText("0");
                            inputFromCrypto.setText("0");
                        }

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setTotalAmount();
            }
        });
    }

    private void setupInputFromCrypto(){

        setupOnFocusListnere(inputFromCrypto);

        inputFromCrypto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int i, int i1, int i2) {
//                if (!text.toString().isEmpty()){
//                    inputFromCrypto.setSelection(text.length());
//                }
            }

            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {
                if (inputFromCrypto != null) {
                    if (getActivity().getCurrentFocus() == inputFromCrypto) {
                        if (!text.toString().isEmpty()) {
                            if (isParsable(text.toString())) {
                                checkForPointAndZeros(text.toString(), inputFromCrypto);
                                double raw = Double.parseDouble(text.toString());
                                if (viewModel.getExchangeRate().getValue() != null) {
                                    double receiveCryptoValue = raw * viewModel.getExchangeRate().getValue();
                                    double currRate = viewModel.getCurrenciesRate();
                                    double sendFiatValue = raw * currRate;

                                    inputToCrypto.setText(NumberFormatter.getInstance().format(receiveCryptoValue));

                                    inputFromFiat.setText(NumberFormatter.getFiatInstance().format(sendFiatValue));
                                    checkMaxLengthBeforePoint(inputFromCrypto, 10, i, i1, i2);

                                    checkForCanSpend(inputFromCrypto, raw);
                                }
                            }
                        } else {
                            //TODO setup zeros
                            inputFromFiat.setText("0");
                            inputToCrypto.setText("0");
//                        inputFromCrypto.setText("0");
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setTotalAmount();
            }
        });
    }

    private void setupInputToCrypto(){
        setupOnFocusListnere(inputToCrypto);

        inputToCrypto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int i, int i1, int i2) {
//                if (!text.toString().isEmpty()){
//                    inputToCrypto.setSelection(text.length());
//                }
            }

            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {
                if (inputToCrypto != null) {
                    if (getActivity().getCurrentFocus() == inputToCrypto) {
                        if (!text.toString().isEmpty()) {
                            if (isParsable(text.toString())) {
                                checkForPointAndZeros(text.toString(), inputToCrypto);
                                double raw = Double.parseDouble(text.toString());
                                double sendCryptoValue = raw / viewModel.getExchangeRate().getValue();
                                double currRate = viewModel.getCurrenciesRate();
                                double sendFiatValue = sendCryptoValue * currRate;
                                inputFromCrypto.setText(NumberFormatter.getInstance().format(sendCryptoValue));
                                inputFromFiat.setText(NumberFormatter.getFiatInstance().format(sendFiatValue));
                                checkMaxLengthBeforePoint(inputToCrypto, 10, i, i1, i2);
                                checkForCanSpend(inputToCrypto, sendCryptoValue);
                            }
                        } else {
                            inputFromFiat.setText("0");
//                        inputToCrypto.setText("0");
                            inputFromCrypto.setText("0");
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setTotalAmount();
            }
        });


    }

    private void setTotalAmount(){
        String fromCryptoAmount = inputFromCrypto.getText().toString();

        if (!fromCryptoAmount.isEmpty() && isParsable(fromCryptoAmount) && Float.parseFloat(fromCryptoAmount) > 0){
            viewModel.estimateTransaction(fromCryptoAmount);
        } else {
            setSummeryToZero();
        }
    }

    private void checkForCanSpend(TextInputEditText input, double amount){
        if (!viewModel.canSpendAmount(amount)){
            int substringCount = input.getText().length() - 1;
            input.setText(input.getText().subSequence(0, substringCount < 0 ? 0 : substringCount));
            input.setSelection(input.getText().length());
            viewModel.errorMessage.setValue(getString(R.string.enter_sum_too_much));


        }
    }

    private void setupPayFromWallet(){

        //TODO add check for ERC20 TOken send or BTC/ETH send
        if (viewModel.getSendERC20Token().getValue() != null){
            viewModel.getSendERC20Token().observe(this, token ->{
                Wallet parentWallet = viewModel.getPayFromWallet().getValue();
                if (parentWallet != null){
                    tvPayFromWallet.setText(parentWallet.getWalletName());

                    Picasso.get().load(token.getLogo()).into(ibPayFromWalletLogo);

                    inputFromCryptoLayout.setHint(token.getName());
                    tvSummeryCrypto.setText(token.getName());
                    Picasso.get().load(token.getLogo()).into(ivSummeryFromLogo);

                    tvSummeryFromFiat.setVisibility(View.INVISIBLE);
                    inputFromCryptoLayout.setVisibility(View.INVISIBLE);
                    inputFromFiatLayout.setVisibility(View.INVISIBLE);
                    buttonMax.setVisibility(View.INVISIBLE);
                    tvExchangeRate.setVisibility(View.INVISIBLE);
                    tvExchangeMin.setVisibility(View.INVISIBLE);

                    //TODO add additional check for that
                    tvExchangeTap.setVisibility(View.VISIBLE);
                    tilExchangeToCryptoLayout.setVisibility(View.INVISIBLE);
                    inputToFiatLayout.setVisibility(View.INVISIBLE);
                    hideTotalSummery();
                }
            });

        } else {
            viewModel.getPayFromWallet().observe(this, wallet -> {
                if (wallet != null){
                    tvPayFromWallet.setText(wallet.getWalletName());
                    ibPayFromWalletLogo.setImageResource(wallet.getIconResourceId());
                    inputFromCryptoLayout.setHint(wallet.getCurrencyName());
                    tvSummeryCrypto.setText(wallet.getCurrencyName());
                    ivSummeryFromLogo.setImageResource(wallet.getIconResourceId());
                    tvSummeryFromFiat.setText("$ 0.0");
                    inputFromCryptoLayout.setVisibility(View.INVISIBLE);
                    inputFromFiatLayout.setVisibility(View.INVISIBLE);
                    buttonMax.setVisibility(View.INVISIBLE);
                    tvExchangeRate.setVisibility(View.INVISIBLE);
                    tvExchangeMin.setVisibility(View.INVISIBLE);

                    //TODO add additional check for that
                    tvExchangeTap.setVisibility(View.VISIBLE);
                    tilExchangeToCryptoLayout.setVisibility(View.INVISIBLE);
                    inputToFiatLayout.setVisibility(View.INVISIBLE);
                    hideTotalSummery();
//                tvSummeryToWallet.setVisibility(View.INVISIBLE);
//                tvSummeryReceiveCrypto.setVisibility(View.INVISIBLE);
//                tvSummeryReceiveFiat.setVisibility(View.INVISIBLE);
//                ivSummeryToLogo.setVisibility(View.INVISIBLE);
//                tvSummeryToTitle.setVisibility(View.INVISIBLE);
                }
//            Log.d("EXCHANGE FRAGMENT", "GOT WALLET NAME:"+wallet.getCurrencyId() + " CURRENCIE ID:"+ wallet.getCurrencyName());
            });
        }

        viewModel.getReceiveToWallet().observe(this, wallet -> {
            tvExchangeTap.setVisibility(View.GONE);
            tvSummeryToTitle.setVisibility(View.VISIBLE);

            tilExchangeToCryptoLayout.setHint(viewModel.getSelectedAsset().getValue().getName());

            //TODO complete this logic in the next iteration
//            if (viewModel.haveToCryptoRate()){
//                tilExchangeToFiatLayout.setVisibility(View.VISIBLE);
//            } else {
//                tilExchangeToFiatLayout.setVisibility(View.INVISIBLE);
//            }
            inputToFiatLayout.setVisibility(View.INVISIBLE);



            tilExchangeToCryptoLayout.setVisibility(View.VISIBLE);

            showTotalSummery();
            tvSummeryToWallet.setText(wallet.getWalletName());
            tvSummeryToWallet.setVisibility(View.VISIBLE);


            tvSummeryReceiveCrypto.setText(viewModel.getSelectedAsset().getValue().getName());
            tvSummeryReceiveCrypto.setVisibility(View.VISIBLE);

            //TODO UPDATE THIS TO CURRECT VALUE
            tvSummeryReceiveFiat.setVisibility(View.INVISIBLE);

            Picasso.get().load(viewModel.getSelectedAsset().getValue().getLogo()).into(ivSummeryToLogo);
//            ivSummeryToLogo.setImageResource(wallet.getIconResourceId());
            ivSummeryToLogo.setVisibility(View.VISIBLE);

            Picasso.get().load(viewModel.getSelectedAsset().getValue().getLogo()).into(ibReceiveToLogo);

            buttonMax.setText("MAX" +  viewModel.getReceiveToWallet().getValue().getBalanceLabelTrimmed());
            buttonMax.setVisibility(View.VISIBLE);


            tvExchangeRate.setVisibility(View.INVISIBLE);
            tvExchangeMin.setVisibility(View.INVISIBLE);

        });
        viewModel.getExchangeRate().observe(this, rate ->{

            inputFromCryptoLayout.setVisibility(View.VISIBLE);


            if(viewModel.haveFromCryptoRate()){
                inputFromFiatLayout.setVisibility(View.VISIBLE);
            } else {
                inputFromFiatLayout.setVisibility(View.INVISIBLE);
            }

            tvExchangeRate.setVisibility(View.VISIBLE);

            String from = null;
            if (viewModel.getSendERC20Token().getValue() != null){
                from = viewModel.getSendERC20Token().getValue().getName();
            } else {
                from = viewModel.getPayFromWallet().getValue().getCurrencyName();
            }
            tvExchangeRate.setText(String.format("1 %s = %s %s", from, NumberFormatter.getInstance().format(rate), viewModel.getSelectedAsset().getValue().getName()));
        });


        viewModel.getMinAmount().observe(this, minValue ->{
            String from = null;
            if (viewModel.getSendERC20Token().getValue()!= null){
                from = viewModel.getSendERC20Token().getValue().getName();
            } else {
                from = viewModel.getPayFromWallet().getValue().getCurrencyName();
            }
            tvExchangeMin.setText(String.format("MIN %s %s", NumberFormatter.getInstance().format(minValue),from));
            tvExchangeMin.setVisibility(View.VISIBLE);
        });

        //TODO getExchangePair need to be implemented
//        viewModel.getSelectedAsset().observe(this, asset -> {
//            //TODO setup value from asset
//        });



    }


    private boolean checkAddressForValidation(String address) {
        for (NativeDataHelper.Blockchain blockchain : NativeDataHelper.Blockchain.values()) {
            for (NativeDataHelper.NetworkId networkId : NativeDataHelper.NetworkId.values()) {
                if (checkAddressForValidation(address, blockchain.getValue(), networkId.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkAddressForValidation(String address, int blockchainId, int networkId) {
        try {
            NativeDataHelper.isValidAddress(address, blockchainId, networkId);
            return true;
        } catch (Throwable ignore) {
        }
        return false;
    }

    private void logLaunch() {
        if (getActivity() != null && !getActivity().getIntent().hasExtra(Constants.EXTRA_ADDRESS)) {
            Analytics.getInstance(getActivity()).logSendToLaunch();
        }
    }

//    @OnClick(R.id.button_address)
//    void onClickAddressBook() {
//        Analytics.getInstance(getActivity()).logSendTo(AnalyticsConstants.SEND_TO_ADDRESS_BOOK);
////        if (getActivity() != null) {
////            DonateDialog.getInstance(Constants.DONATE_ADDING_CONTACTS)
////                    .show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
////        }
//        ContactsFragment fragment = ContactsFragment.newInstance();
//        fragment.setTargetFragment(this, REQUEST_CONTACT);
//        if (getActivity() != null && getView() != null) {
//            getActivity().getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container_full, fragment, ContactsFragment.TAG)
//                    .addToBackStack(ContactsFragment.TAG)
//                    .commit();
//        }
//    }
//
//    @OnClick(R.id.button_scan_wireless)
//    void onClickWirelessScan() {
//        Analytics.getInstance(getActivity()).logSendTo(AnalyticsConstants.SEND_TO_WIRELESS);
//        startActivity(new Intent(getActivity(), MagicSendActivity.class));
//    }
//
//    @OnClick(R.id.button_scan_qr)
//    void onClickScanQr() {
//        Analytics.getInstance(getActivity()).logSendTo(AnalyticsConstants.SEND_TO_QR);
//        ((AssetSendActivity) getActivity()).showScanScreen();
//    }
//
//    @OnClick(R.id.button_next)
//    void onClickNext() {
//        viewModel.setReceiverAddress(inputAddress.getText().toString());
//        viewModel.thoseAddress.setValue(inputAddress.getText().toString());
////        ((AssetSendActivity) getActivity()).setFragment(R.string.send_from, R.id.container, WalletChooserFragment.newInstance(blockchainId, networkId));
//
//        if (getActivity() instanceof TokenSendActivity) {
//            ((TokenSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, EthTransactionFeeFragment.newInstance());
//            return;
//        }
//
//        if (getActivity().getIntent().hasCategory(Constants.EXTRA_SENDER_ADDRESS)) {
//            RealmManager.getAssetsDao().getWalletById(getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0));
//            if (viewModel.getWallet().getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()) {
//                ((AssetSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, TransactionFeeFragment.newInstance());
//            } else if (viewModel.getWallet().getCurrencyId() == NativeDataHelper.Blockchain.ETH.getValue()) {
//                ((AssetSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, EthTransactionFeeFragment.newInstance());
//            } else if (viewModel.getWallet().getCurrencyId() == NativeDataHelper.Blockchain.EOS.getValue()) {
//                ((AssetSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, AmountChooserFragment.newInstance());
//            }
//        }
//    }
//
//    @OnClick(R.id.container)
//    void onClickContainer() {
//        if (getActivity() != null) {
//            hideKeyboard(getActivity());
//        }
//    }




    private void checkForPointAndZeros(String input, EditText inputView) {
        if (inputView != null) {
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
            Float.parseFloat(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private void setupOnFocusListnere(TextInputEditText input){

        input.setOnTouchListener((v, motionEvent) ->{
            String text = input.getText().toString();
            if (!text.isEmpty() && text.length() > 0){
                if (!text.equals("0"))
                    input.setSelection(text.length());
            }

            if (!input.hasFocus()) {
                input.requestFocus();

                showKeyboard(getActivity(), input);

                return true;
            } else {
                showKeyboard(getActivity(), input);
            }
            return true;
        });

        input.setOnFocusChangeListener((view, hasFocus) -> {
            String text = input.getText().toString();
            if (hasFocus){

                if (!text.isEmpty()){
                    if (text.equals("0")){
                        input.setText("");

                    }
//                    else {
//                        input.setSelection(text.length());
//                    }
                }

            } else {
                if (text.isEmpty()){
                    input.setText("0");
                }
                hideKeyboard(getActivity());
                    //TODO total summery should be visible

            }
        });
    }

    private void hideTotalSummery(){
        Log.d("EXCHANGE FRAGMENT", "TOTAL SUMMERY SHOULD GONE");
        if (summeryLayout!= null)
            summeryLayout.setVisibility(View.GONE);
    }

    private void showTotalSummery(){
        Log.d("EXCHANGE FRAGMENT", "TOTAL SUMMERY SHOULD VISIBLE");
        if (summeryLayout!= null)
            summeryLayout.setVisibility(View.VISIBLE);
    }

    private void setSummeryToZero(){
        tvSummeryCrypto.setText(String.format("0 %s", viewModel.getPayFromWallet().getValue().getCurrencyName()));
        tvSummeryFromFiat.setText("$ 0");
        tvSummeryReceiveCrypto.setText(String.format("0 %s", viewModel.getSelectedAsset().getValue().getName()));
    }
}
