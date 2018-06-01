/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.RecentAddress;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.adapters.RecentAddressesAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.DonateDialog;
import io.multy.ui.fragments.send.ethereum.EthTransactionFeeFragment;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetSendViewModel;
import io.realm.RealmResults;

public class AssetSendFragment extends BaseFragment {

    public static AssetSendFragment newInstance(){
        return new AssetSendFragment();
    }

    @BindView(R.id.input_address)
    EditText inputAddress;
    @BindView(R.id.button_next)
    TextView buttonNext;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private int blockchainId;
    private int networkId;
    private AssetSendViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asset_send, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        setBaseViewModel(viewModel);
        viewModel.getReceiverAddress().observe(getActivity(), s -> {
            inputAddress.setText(s);
            inputAddress.setSelection(inputAddress.length());
        });
        setupInputAddress();
        initRecentAddresses();
        logLaunch();
        return view;
    }

    private void initRecentAddresses() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        RealmResults<RecentAddress> recentAddresses;
        if (getActivity() != null && getActivity().getIntent().hasCategory(Constants.EXTRA_SENDER_ADDRESS)
                && viewModel.getWallet() != null) {
            recentAddresses = RealmManager.getAssetsDao()
                    .getRecentAddresses(viewModel.getWallet().getCurrencyId(), viewModel.getWallet().getNetworkId());
        } else {
            recentAddresses = RealmManager.getAssetsDao().getRecentAddresses();
        }
        recyclerView.setAdapter(new RecentAddressesAdapter(recentAddresses, address -> {
            inputAddress.setText(address);
            inputAddress.setSelection(inputAddress.length());
            inputAddress.postDelayed(this::onClickNext, 300);
        }));
    }

    @Override
    public void onDestroyView() {
        hideKeyboard(getActivity());
        super.onDestroyView();
    }

    private void setupInputAddress() {
        inputAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isValidAddress = getActivity() != null &&
                        getActivity().getIntent().hasCategory(Constants.EXTRA_SENDER_ADDRESS) && viewModel.getWallet() != null ?
                        checkAddressForValidation(charSequence.toString(),
                                viewModel.getWallet().getCurrencyId(), viewModel.getWallet().getNetworkId()) :
                        checkAddressForValidation(charSequence.toString());
                if (TextUtils.isEmpty(charSequence) || !isValidAddress){
                    buttonNext.setBackgroundResource(R.color.disabled);
                    buttonNext.setEnabled(false);
                } else {
                    buttonNext.setBackgroundResource(R.drawable.btn_gradient_blue);
                    buttonNext.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
            AssetSendFragment.this.blockchainId = blockchainId;
            AssetSendFragment.this.networkId = networkId;
            return true;
        } catch (Throwable ignore) { }
        return false;
    }

    private void logLaunch() {
        if (getActivity() != null && !getActivity().getIntent().hasExtra(Constants.EXTRA_ADDRESS)) {
            Analytics.getInstance(getActivity()).logSendToLaunch();
        }
    }

    @OnClick(R.id.button_address)
    void onClickAddressBook(){
        Analytics.getInstance(getActivity()).logSendTo(AnalyticsConstants.SEND_TO_ADDRESS_BOOK);
        if (getActivity() != null) {
            DonateDialog.getInstance(Constants.DONATE_ADDING_CONTACTS)
                    .show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
        }
    }

    @OnClick(R.id.button_scan_wireless)
    void onClickWirelessScan(){
        Analytics.getInstance(getActivity()).logSendTo(AnalyticsConstants.SEND_TO_WIRELESS);
        if (getActivity() != null) {
            DonateDialog.getInstance(Constants.DONATE_ADDING_WIRELESS_SCAN)
                    .show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
        }
    }

    @OnClick(R.id.button_scan_qr)
    void onClickScanQr(){
        Analytics.getInstance(getActivity()).logSendTo(AnalyticsConstants.SEND_TO_QR);
        ((AssetSendActivity) getActivity()).showScanScreen();
    }

    @OnClick(R.id.button_next)
    void onClickNext(){
        viewModel.setReceiverAddress(inputAddress.getText().toString());
        viewModel.thoseAddress.setValue(inputAddress.getText().toString());
        ((AssetSendActivity) getActivity()).setFragment(R.string.send_from, R.id.container, WalletChooserFragment.newInstance(blockchainId, networkId));
        if (getActivity().getIntent().hasCategory(Constants.EXTRA_SENDER_ADDRESS)) {
            RealmManager.getAssetsDao().getWalletById(getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0));
            if (viewModel.getWallet().getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()) {
                ((AssetSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, TransactionFeeFragment.newInstance());
            } else if (viewModel.getWallet().getCurrencyId() == NativeDataHelper.Blockchain.ETH.getValue()) {
                ((AssetSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, EthTransactionFeeFragment.newInstance());
            }
        }
    }

    @OnClick(R.id.container)
    void onClickContainer() {
        if (getActivity() != null) {
            hideKeyboard(getActivity());
        }
    }
}
