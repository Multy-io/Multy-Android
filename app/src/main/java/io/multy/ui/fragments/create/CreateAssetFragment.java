package io.multy.ui.fragments.create;
/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.ListDialogFragment;
import io.multy.util.Constants;
import io.multy.util.CurrencyType;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.viewmodels.WalletViewModel;

/**
 * Created by anschutz1927@gmail.com on 23.11.17.
 */

public class CreateAssetFragment extends BaseFragment {

    public static final String TAG = CreateAssetFragment.class.getSimpleName();

    @BindView(R.id.edit_name)
    EditText editTextWalletName;
    @BindView(R.id.text_create)
    TextView textViewCreateWallet;
    @BindView(R.id.text_fiat_currency)
    TextView textViewFiatCurrency;
    @BindView(R.id.text_chain_currency)
    TextView textViewChainCurrency;

    private WalletViewModel walletViewModel;

    public static CreateAssetFragment newInstance() {
        return new CreateAssetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_assets_action_add, container, false);
        ButterKnife.bind(this, v);
        if (getActivity().getWindow() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE |
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        initialize();
        subscribeToCurrencyUpdate();
        return v;
    }

    private void subscribeToCurrencyUpdate() {
        walletViewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        walletViewModel.fiatCurrency.observe(this, s -> {
            //TODO update wallet fiat currency
            textViewFiatCurrency.setText(s);
        });
        walletViewModel.chainCurrency.observe(this, s -> {
            //TODO update wallet chain currency
            textViewChainCurrency.setText(s);
        });
    }

    private void initialize() {
        editTextWalletName.addTextChangedListener(getEditWatcher());
    }

    private TextWatcher getEditWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    if (textViewChainCurrency.getText().length() > 0 && textViewFiatCurrency.getText().length() > 0) {
                        textViewCreateWallet.setEnabled(true);
                        textViewCreateWallet.setBackgroundColor(Color.parseColor("#FF459FF9"));
                    }
                } else {
                    textViewCreateWallet.setEnabled(false);
                    textViewCreateWallet.setBackgroundColor(Color.parseColor("#BEC8D2"));
                }
            }
        };
    }

    @OnClick(R.id.button_chain)
    public void onClickChain() {
        ArrayList<String> chains = new ArrayList<>(2);
        chains.add(Constants.BTC);
        chains.add(Constants.ETH);
        ListDialogFragment.newInstance(chains, CurrencyType.CHAIN).show(getFragmentManager(), "");
    }

    @OnClick(R.id.button_fiat)
    public void onClickFiat() {
        ArrayList<String> chains = new ArrayList<>(3);
        chains.add(Constants.USD);
        chains.add(Constants.EUR);
        ListDialogFragment.newInstance(chains, CurrencyType.FIAT).show(getFragmentManager(), "");
    }

    @OnClick(R.id.text_create)
    public void onClickCreate() {
        WalletRealmObject walletRealmObject = null;
        try {
            List<WalletRealmObject> wallets = new DataManager(Multy.getContext()).getWallets();
            final int index = wallets != null && wallets.size() > 0 ? wallets.size() : 0;
            final int currency = NativeDataHelper.Currency.BTC.getValue(); //TODO implement choosing crypto currency using enum NativeDataHelper.CURRENCY
            String creationAddress = NativeDataHelper.makeAccountAddress(new DataManager(getActivity()).getSeed().getSeed(), index, 0, currency);
            walletRealmObject = new WalletRealmObject();
            walletRealmObject.setName(editTextWalletName.getText().toString());

            if (textViewChainCurrency.getText().toString().equals(Constants.BTC)) {
                walletRealmObject.setCurrency(0);
            } else {
                walletRealmObject.setCurrency(1);
            }
            walletRealmObject.setAddressIndex(0);
            walletRealmObject.setCreationAddress(creationAddress);
            walletRealmObject.setWalletIndex(index);
            MultyApi.INSTANCE.addWallet(getActivity(), walletRealmObject);
        } catch (JniException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(getContext(), AssetActivity.class);
        if (walletRealmObject != null) {
            intent.putExtra(Constants.EXTRA_WALLET_ID, walletRealmObject.getWalletIndex());
        }

        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.text_cancel)
    void onCancelClick() {
        getActivity().finish();
    }
}
