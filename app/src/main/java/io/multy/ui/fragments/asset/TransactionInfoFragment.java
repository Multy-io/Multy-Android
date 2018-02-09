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

import com.samwolfand.oneprefs.Prefs;

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
    @BindView(R.id.back)
    View buttonBack;
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
    private int walletIndex;

    public static TransactionInfoFragment newInstance(Bundle transactionInfoMode) {
        TransactionInfoFragment fragment = new TransactionInfoFragment();
        fragment.setArguments(transactionInfoMode);
        return fragment;
    }

    public TransactionInfoFragment() {}

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
        initialize();
        return v;
    }

    private void initialize() {
        if (getArguments() == null) {
            return;
        }
        selectedPosition = getArguments().getInt(SELECTED_POSITION, 0);
        this.walletIndex = getArguments().getInt(WALLET_INDEX);
        int mode = getArguments().getInt(TRANSACTION_INFO_MODE, 0);
        if (mode == MODE_RECEIVE) {
            parent.setBackgroundColor(colorGreen);
            imageOperation.setImageResource(R.drawable.ic_receive_big_new);
        } else if (mode == MODE_SEND) {
            parent.setBackgroundColor(colorBlue);
            imageOperation.setImageResource(R.drawable.ic_send_big);
        } else {
            return;
        }
        buttonBack.setOnClickListener(view -> getActivity().onBackPressed());
        loadData();
    }

    private void loadData() {
        viewModel.getWalletLive().observe(this, walletRealmObject -> {
            if (walletRealmObject != null) {
                toolbarWalletName.setText(walletRealmObject.getName());
            }
        });
        viewModel.getTransactionsHistory().observe(this, transactionHistories -> {
            if (transactionHistories == null || transactionHistories.size() == 0) {
                return;
            } else if (transaction != null) {
                setData();
                return;
            }
            transaction = transactionHistories.get(selectedPosition);
            setData();
        });
    }

    private void setData() {
        boolean isIncoming = transaction.getTxStatus() == TX_IN_BLOCK_INCOMING ||
                transaction.getTxStatus() == TX_CONFIRMED_INCOMING ||
                transaction.getTxStatus() == TX_MEMPOOL_INCOMING;
        textDate.setText(DateHelper.DATE_FORMAT_TRANSACTION_INFO.format(transaction.getBlockTime() * 1000));
        if (isIncoming) {
            textValue.setText("+");
            textAmount.setText("+");
            textValue.append(CryptoFormatUtils.satoshiToBtc(transaction.getTxOutAmount()));
            if (transaction.getStockExchangeRates() != null && transaction.getStockExchangeRates().size() > 0) {
                textAmount.append(CryptoFormatUtils.satoshiToUsd(transaction.getTxOutAmount(), transaction.getStockExchangeRates().get(0).getExchanges().getBtcUsd()));
                textMoney.setVisibility(View.VISIBLE);
            } else {
                textAmount.setText("");
                textMoney.setVisibility(View.GONE);
            }
        } else {
            textValue.setText("-");
            textAmount.setText("-");

            WalletAddress addressTo = null;
            List<WalletAddress> outputs = transaction.getOutputs();
            for (WalletAddress output : outputs) {
                if (!output.getAddress().equals(Constants.DONTAION_ADDRESS)) {
                    for (WalletAddress walletAddress : RealmManager.getAssetsDao().getWalletById(walletIndex).getAddresses()) {
                        if (!output.getAddress().equals(walletAddress.getAddress())) {
                            addressTo = output;
                        }
                    }
                }
            }
            if (addressTo != null) {
                textValue.append(CryptoFormatUtils.satoshiToBtc(addressTo.getAmount()));
                if (transaction.getStockExchangeRates() != null && transaction.getStockExchangeRates().size() > 0) {
                    textAmount.append(CryptoFormatUtils.satoshiToUsd(addressTo.getAmount(), transaction.getStockExchangeRates().get(0).getExchanges().getBtcUsd()));
                    textMoney.setVisibility(View.VISIBLE);
                } else {
                    textAmount.setText("");
                    textMoney.setVisibility(View.GONE);
                }
            }

            getServerConfig();
        }
        String addressesfrom = "";
        for (WalletAddress singleAddress : transaction.getInputs()) {
            addressesfrom = addressesfrom.concat(System.lineSeparator()).concat(singleAddress.getAddress());
        }
        addressesfrom = addressesfrom.substring(1);
        textAdressesFrom.setText(addressesfrom);
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
                blocks = getString(R.string.in_mempool);
                break;
            case TX_IN_BLOCK_INCOMING:
            case TX_IN_BLOCK_OUTCOMING:
                blocks = "1 - 6 " + getString(R.string.confirmation);
                break;
            case TX_CONFIRMED_INCOMING:
            case TX_CONFIRMED_OUTCOMING:
                blocks = "6+ " + getString(R.string.confirmation);
                break;
            default:
                blocks = getString(R.string.no_information);
        }
        textConfirmations.setText(blocks);
        txid = transaction.getTxId();
    }

    private void getServerConfig() {
        String btcDonateAddress = Prefs.getString(Constants.PREF_DONATE_ADDRESS_BTC, "no address");
        try {
            for (WalletAddress address : transaction.getOutputs()) {
                if (address.getAddress().equals(btcDonateAddress)) {
                    initializeDonationBlock(address);
                    break;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void initializeDonationBlock(WalletAddress address) {
        viewDonate.setVisibility(View.VISIBLE);
        textDonateValue.setText(CryptoFormatUtils.satoshiToBtc(address.getAmount()));
        if (transaction.getStockExchangeRates() != null && transaction.getStockExchangeRates().size() > 0) {
            textDonateAmount.setText(CryptoFormatUtils.satoshiToUsd(address.getAmount(), transaction.getStockExchangeRates().get(0).getExchanges().getBtcUsd()));
            textDonateMoney.setVisibility(View.VISIBLE);
        } else {
            textDonateAmount.setText("");
            textDonateMoney.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.button_view)
    void onViewClick(View view) {
        try {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true), 1500);
            String url = Constants.BLOCKCHAIN_INFO_PATH + txid;
            ((BaseActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_full, WebFragment.newInstance(url))
                    .addToBackStack(TransactionInfoFragment.TAG)
                    .commit();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
