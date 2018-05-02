/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.TransactionHistory;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.BaseActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.WebFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.DateHelper;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.WalletViewModel;

import static io.multy.util.Constants.TX_CONFIRMED_INCOMING;
import static io.multy.util.Constants.TX_CONFIRMED_OUTCOMING;
import static io.multy.util.Constants.TX_IN_BLOCK_INCOMING;
import static io.multy.util.Constants.TX_IN_BLOCK_OUTCOMING;
import static io.multy.util.Constants.TX_MEMPOOL_INCOMING;
import static io.multy.util.Constants.TX_MEMPOOL_OUTCOMING;

/**
 * Created by anschutz1927@gmail.com on 16.01.18.
 */

public class TransactionInfoFragment extends BaseFragment {

    public static final String TAG = TransactionInfoFragment.class.getSimpleName();
    public static final String SELECTED_POSITION = "selectedposition";
    public static final String TRANSACTION_INFO_MODE = "mode";
    public static final String WALLET_INDEX = "walletindex";
    public static final int MODE_RECEIVE = 1;
    public static final int MODE_SEND = 2;

    @BindView(R.id.linear_parent)
    LinearLayout parent;
    @BindView(R.id.toolbar_name)
    TextView toolbarWalletName;
    @BindView(R.id.text_date)
    TextView textDate;
    @BindView(R.id.image_operation)
    ImageView imageOperation;
    @BindView(R.id.text_value)
    TextView textValue;
    @BindView(R.id.text_coin)
    TextView textCoin;
    @BindView(R.id.text_amount)
    TextView textAmount;
    @BindView(R.id.text_money)
    TextView textMoney;
    @BindView(R.id.text_comment)
    TextView textComment;
    @BindView(R.id.text_addresses_from)
    TextView textAdressesFrom;
    @BindView(R.id.arrow)
    ImageView imageArrow;
    @BindView(R.id.logo)
    ImageView imageCoinLogo;
    @BindView(R.id.text_addresses_to)
    TextView textAddressesTo;
    @BindView(R.id.text_confirmations)
    TextView textConfirmations;
    @BindView(R.id.button_view)
    TextView buttonView;
    @BindView(R.id.donat_include)
    View viewDonate;
    @BindView(R.id.donat_value)
    TextView textDonateValue;
    @BindView(R.id.donat_amount)
    TextView textDonateAmount;
    @BindView(R.id.donat_money)
    TextView textDonateMoney;
    @BindColor(R.color.green_light)
    int colorGreen;
    @BindColor(R.color.blue_sky)
    int colorBlue;

    private WalletViewModel viewModel;
    TransactionHistory transaction;
    private int selectedPosition;
    private String txid;
    private boolean isTransactionLogged;
    private List<String> walletAddresses = new ArrayList<>();
    private int networkId = 0;
    private String donateAddress;

    public static TransactionInfoFragment newInstance(Bundle transactionInfoMode) {
        TransactionInfoFragment fragment = new TransactionInfoFragment();
        fragment.setArguments(transactionInfoMode);
        return fragment;
    }

    public TransactionInfoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = getLayoutInflater().inflate(R.layout.fragment_transaction_info, container, false);
        ButterKnife.bind(this, v);
        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        isTransactionLogged = false;
        initialize();
        return v;
    }

    private void initialize() {
        if (getArguments() == null) {
            return;
        }
        selectedPosition = getArguments().getInt(SELECTED_POSITION, 0);
        int mode = getArguments().getInt(TRANSACTION_INFO_MODE, 0);
        parent.setBackgroundColor(mode == MODE_RECEIVE ? colorGreen : colorBlue);
        imageOperation.setImageResource(mode == MODE_RECEIVE ? R.drawable.ic_receive_big_new : R.drawable.ic_send_big);
        loadData();
    }

    private void loadData() {
        viewModel.getWalletLive().observe(this, wallet -> {
            if (wallet != null) {
                imageCoinLogo.setImageResource(wallet.getNetworkId() == NativeDataHelper.NetworkId.MAIN_NET.getValue() ?
                        R.drawable.ic_btc_huge : R.drawable.ic_chain_btc_test);
                toolbarWalletName.setText(wallet.getWalletName());
                for (WalletAddress address : wallet.getBtcWallet().getAddresses()) {
                    if (!walletAddresses.contains(address.getAddress())) {
                        walletAddresses.add(address.getAddress());
                    }
                }
                loadTransactionHistory(wallet.getCurrencyId(), wallet.getNetworkId(), wallet.getIndex());
            }
        });

    }

    private void loadTransactionHistory(final int currencyId, final int networkId, final int walletIndex) {
        this.networkId = networkId;
        viewModel.getTransactionsHistory(currencyId, networkId, walletIndex).observe(this, transactionHistories -> {
            if (transactionHistories == null || transactionHistories.size() == 0) {
                return;
            } else if (transaction != null) {
                setData();
                return;
            }
            donateAddress = RealmManager.getSettingsDao().getDonationAddress(Constants.DONATE_WITH_TRANSACTION);
            transaction = transactionHistories.get(selectedPosition);
            setData();
        });
    }

    private long getOutComingAmount(TransactionHistory transactionHistory, List<String> walletAddresses, double exchangeRate) {
        long totalAmount = 0;
        long outAmount = 0;

        for (WalletAddress walletAddress : transactionHistory.getInputs()) {
            totalAmount += walletAddress.getAmount();
        }

        for (WalletAddress walletAddress : transactionHistory.getOutputs()) {
            if (walletAddresses.contains(walletAddress.getAddress())) {
                outAmount += walletAddress.getAmount();
            } else if (isDonationAddress(walletAddress.getAddress())) {
                initializeDonationBlock(walletAddress, exchangeRate);
            }
        }

        return totalAmount - outAmount;
    }

    private void setData() {
        boolean isIncoming = transaction.getTxStatus() == TX_IN_BLOCK_INCOMING ||
                transaction.getTxStatus() == TX_CONFIRMED_INCOMING ||
                transaction.getTxStatus() == TX_MEMPOOL_INCOMING;
        double exchangeRate = getPreferredExchangeRate(transaction.getStockExchangeRates());
        if (isIncoming) {
            textValue.setText("+");
            textAmount.setText("+");
            textValue.append(CryptoFormatUtils.satoshiToBtc(transaction.getTxOutAmount()));
            textAmount.append(CryptoFormatUtils.satoshiToUsd(transaction.getTxOutAmount(), exchangeRate));
        } else {
            textValue.setText("-");
            textAmount.setText("-");
            long outValue = getOutComingAmount(transaction, walletAddresses, exchangeRate);
            textValue.append(CryptoFormatUtils.satoshiToBtc(outValue));
            textAmount.append(CryptoFormatUtils.satoshiToUsd(outValue, exchangeRate));
        }
        String addressesFrom = "";
        for (WalletAddress singleAddress : transaction.getInputs()) {
            addressesFrom = addressesFrom.concat(System.lineSeparator()).concat(singleAddress.getAddress());
        }
        addressesFrom = addressesFrom.substring(1);
        textAdressesFrom.setText(addressesFrom);
        String addressesTo = "";
        for (WalletAddress singleAddress : transaction.getOutputs()) {
            addressesTo = addressesTo.concat(System.lineSeparator()).concat(singleAddress.getAddress());
        }
        addressesTo = addressesTo.substring(1);
        textAddressesTo.setText(addressesTo);
        String blocks;
        switch (transaction.getTxStatus()) {
            case TX_MEMPOOL_INCOMING:
            case TX_MEMPOOL_OUTCOMING:
                textDate.setText(DateHelper.DATE_FORMAT_TRANSACTION_INFO.format(transaction.getMempoolTime() * 1000));
                blocks = getString(R.string.in_mempool);
                break;
            case TX_IN_BLOCK_INCOMING:
            case TX_IN_BLOCK_OUTCOMING:
                textDate.setText(DateHelper.DATE_FORMAT_TRANSACTION_INFO.format(transaction.getBlockTime() * 1000));
                blocks = "1 - 6 " + getString(R.string.confirmation);
                break;
            case TX_CONFIRMED_INCOMING:
            case TX_CONFIRMED_OUTCOMING:
                textDate.setText(DateHelper.DATE_FORMAT_TRANSACTION_INFO.format(transaction.getBlockTime() * 1000));
                blocks = "6+ " + getString(R.string.confirmation);
                break;
            default:
                blocks = getString(R.string.no_information);
        }
        textConfirmations.setText(blocks);
        txid = transaction.getTxId();
        if (!isTransactionLogged) {
            logTransaction(transaction.getTxStatus(), AnalyticsConstants.WALLET_TRANSACTIONS_SCREEN);
            isTransactionLogged = true;
        }
    }

    private boolean isDonationAddress(String address) {
        if (networkId == NativeDataHelper.NetworkId.TEST_NET.getValue()) {
            return address.equals(Constants.DONATION_ADDRESS_TESTNET);
        }
        return RealmManager.getSettingsDao().isDonateAddress(address);
    }

    private double getPreferredExchangeRate(ArrayList<TransactionHistory.StockExchangeRate> stockExchangeRates) {
        if (stockExchangeRates != null && stockExchangeRates.size() > 0) {
            for (TransactionHistory.StockExchangeRate rate : stockExchangeRates) {
                if (rate.getExchanges().getBtcUsd() > 0) {
                    return rate.getExchanges().getBtcUsd();
                }
            }
        }
        return 0.0;
    }

    private void initializeDonationBlock(WalletAddress address, double exchangeRate) {
        viewDonate.setVisibility(View.VISIBLE);
        textDonateValue.setText(CryptoFormatUtils.satoshiToBtc(address.getAmount()));
        textDonateAmount.setText(CryptoFormatUtils.satoshiToUsd(address.getAmount(), exchangeRate));
    }

    private int getLogStatus() {
        switch (transaction.getTxStatus()) {
            case TX_MEMPOOL_INCOMING:
            case TX_MEMPOOL_OUTCOMING:
                return 0;
            case TX_IN_BLOCK_INCOMING:
            case TX_CONFIRMED_INCOMING:
                return 1;
            case TX_IN_BLOCK_OUTCOMING:
            case TX_CONFIRMED_OUTCOMING:
                return -1;
            default:
                return 0;
        }
    }

    private void logTransaction(int txStatus, String analyticConstant) {
        switch (txStatus) {
            case TX_MEMPOOL_INCOMING:
            case TX_MEMPOOL_OUTCOMING:
                Analytics.getInstance(getActivity()).logWalletTransactionLaunch(analyticConstant, viewModel.getChainId(), 0);
                break;
            case TX_IN_BLOCK_INCOMING:
            case TX_CONFIRMED_INCOMING:
                Analytics.getInstance(getActivity()).logWalletTransactionLaunch(analyticConstant, viewModel.getChainId(), 1);
                break;
            case TX_IN_BLOCK_OUTCOMING:
            case TX_CONFIRMED_OUTCOMING:
                Analytics.getInstance(getActivity()).logWalletTransactionLaunch(analyticConstant, viewModel.getChainId(), -1);
                break;
            default:
        }
    }

    @OnClick(R.id.button_view)
    void onClickView(View view) {
        Analytics.getInstance(getActivity()).logWalletTransactionBlockchain(AnalyticsConstants.WALLET_TRANSACTIONS_BLOCKCHAIN,
                viewModel.getChainId(), getLogStatus());
        try {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true), 1500);
            String url = (networkId == NativeDataHelper.NetworkId.TEST_NET.getValue() ?
                    Constants.BLOCKCHAIN_TEST_INFO_PATH : Constants.BLOCKCHAIN_MAIN_INFO_PATH) + txid;
            ((BaseActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_full, WebFragment.newInstance(url))
                    .addToBackStack(TransactionInfoFragment.TAG)
                    .commit();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @OnClick(R.id.back)
    void onClickBack() {
        getActivity().onBackPressed();
    }
}
