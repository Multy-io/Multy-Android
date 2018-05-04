/*
 * Copyright 2017 Idealnaya rabota LLC
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
import io.multy.model.entities.wallet.Wallet;
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

public class EthTransactionInfoFragment extends BaseFragment {

    public static final String TAG = EthTransactionInfoFragment.class.getSimpleName();
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
    @BindColor(R.color.green_light)
    int colorGreen;
    @BindColor(R.color.blue_sky)
    int colorBlue;

    private WalletViewModel viewModel;
    TransactionHistory transaction;
    private int selectedPosition;
    private String txHash;
    private boolean isTransactionLogged;
    private int networkId = 0;
    private int mode;

    public static EthTransactionInfoFragment newInstance(Bundle transactionInfoMode) {
        EthTransactionInfoFragment fragment = new EthTransactionInfoFragment();
        fragment.setArguments(transactionInfoMode);
        return fragment;
    }

    public EthTransactionInfoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getArguments().getInt(TRANSACTION_INFO_MODE, 0);
        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = getLayoutInflater().inflate(R.layout.fragment_eth_transaction_info, container, false);
        ButterKnife.bind(this, v);
        isTransactionLogged = false;
        initialize();
        return v;
    }

    private void initialize() {
        if (getActivity() == null || getArguments() == null) {
            return;
        }
        selectedPosition = getArguments().getInt(SELECTED_POSITION, 0);
        parent.setBackgroundColor(mode == MODE_RECEIVE ? colorGreen : colorBlue);
        imageOperation.setImageResource(mode == MODE_RECEIVE ? R.drawable.ic_receive_big_new : R.drawable.ic_send_big);
        viewModel.getWalletLive().observe(getActivity(), this::onWallet);
    }

    private void onWallet(Wallet wallet) {
        if (getActivity() == null || wallet == null) {
            return;
        }
        imageCoinLogo.setImageResource(wallet.getIconResourceId());
        toolbarWalletName.setText(wallet.getWalletName());
        this.networkId = wallet.getNetworkId();
        viewModel.getTransactionsHistory(wallet.getCurrencyId(), networkId, wallet.getIndex())
                .observe(getActivity(), this::onTransactions);
    }

    private void onTransactions(List<TransactionHistory> transactionHistories) {
        if (getActivity() == null || transactionHistories == null || transactionHistories.size() == 0) {
            return;
        } else if (transaction != null) {
            setData();
            return;
        }
        transaction = transactionHistories.get(selectedPosition);
        setData();
    }

    private void setData() {
        final String symbol = mode == MODE_RECEIVE ? "+" : "-";
        textValue.setText(symbol);
        textAmount.setText(symbol);
        double exchangeRate = getPreferredExchangeRate(transaction.getStockExchangeRates());
        textValue.append(String.valueOf(CryptoFormatUtils.weiToEth(String.valueOf(transaction.getTxOutAmount()))));
        textAmount.append(CryptoFormatUtils.ethToUsd(CryptoFormatUtils
                .weiToEth(String.valueOf(transaction.getTxOutAmount())), exchangeRate));
        textAdressesFrom.setText(transaction.getFrom());
        textAddressesTo.setText(transaction.getTo());
        textDate.setText(DateHelper.DATE_FORMAT_TRANSACTION_INFO
                .format((transaction.getTxStatus() == TX_MEMPOOL_INCOMING ||
                        transaction.getTxStatus() == TX_MEMPOOL_OUTCOMING ?
                        transaction.getMempoolTime() : transaction.getBlockTime()) * 1000));
        textConfirmations.setText(String.valueOf(transaction.getConfirmations()).concat(" "));
        textConfirmations.append(transaction.getConfirmations() > 1 ?
                getString(R.string.confirmations) : getString(R.string.confirmation));
        txHash = transaction.getTxHash();
        if (!isTransactionLogged) {
            logTransaction(transaction.getTxStatus(), AnalyticsConstants.WALLET_TRANSACTIONS_SCREEN);
            isTransactionLogged = true;
        }
    }

    private double getPreferredExchangeRate(ArrayList<TransactionHistory.StockExchangeRate> stockExchangeRates) {
        if (stockExchangeRates != null && stockExchangeRates.size() > 0) {
            for (TransactionHistory.StockExchangeRate rate : stockExchangeRates) {
                if (rate.getExchanges().getEthUsd() > 0) {
                    return rate.getExchanges().getEthUsd();
                }
            }
        }
        return 0.0;
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
            String url = (networkId == NativeDataHelper.NetworkId.RINKEBY.getValue() ?
                    Constants.ETHERSCAN_RINKEBY_INFO_PATH : Constants.ETHERSCAN_MAIN_INFO_PATH) + txHash;
            getFragmentManager().beginTransaction().replace(R.id.container_full, WebFragment.newInstance(url))
                    .addToBackStack(EthTransactionInfoFragment.TAG).commit();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @OnClick(R.id.back)
    void onClickBack() {
        getActivity().onBackPressed();
    }
}
