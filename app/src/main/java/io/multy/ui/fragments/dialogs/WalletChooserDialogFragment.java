/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.MyWalletsAdapter;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;

public class WalletChooserDialogFragment extends DialogFragment {

    public static final String TAG = WalletChooserDialogFragment.class.getSimpleName();
    public static final String ARG_PAYABLE = "arg_payable";
    public static final int REQUEST_WALLET_ID = 1024;
    private static final String ARG_CURRENCY_ID = "ARG_CURRENCY_ID";
    private static final String ARG_NETWORK_ID = "ARG_NETWORK_ID";

    private boolean isMainNet = false;
    private boolean exceptMultisig = false;

    public static WalletChooserDialogFragment getInstance() {
        return new WalletChooserDialogFragment();
    }

    public static WalletChooserDialogFragment newInstance(boolean mainNet) {
        WalletChooserDialogFragment walletChooserDialogFragment = new WalletChooserDialogFragment();
        walletChooserDialogFragment.setMainNet(mainNet);
        return walletChooserDialogFragment;
    }

    public static WalletChooserDialogFragment getInstance(int currencyId) {
        Bundle args = new Bundle();
        args.putInt(ARG_CURRENCY_ID, currencyId);
        return getInstance(args);
    }

    public static WalletChooserDialogFragment getInstance(int currencyId, int networkId) {
        Bundle args = new Bundle();
        args.putInt(ARG_CURRENCY_ID, currencyId);
        args.putInt(ARG_NETWORK_ID, networkId);
        return getInstance(args);
    }

    private static WalletChooserDialogFragment getInstance(Bundle args) {
        WalletChooserDialogFragment dialogFragment = new WalletChooserDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    public void exceptMultisig(boolean value) {
        this.exceptMultisig = value;
    }

    private MyWalletsAdapter.OnWalletClickListener listener;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        if (recyclerView.getAdapter() != null && recyclerView.getAdapter() instanceof MyWalletsAdapter &&
                !((MyWalletsAdapter) recyclerView.getAdapter()).isValidData()) {
            setupAdapter();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_wallet_chooser, container, false);
        ButterKnife.bind(this, view);
        setupAdapter();
        if (getActivity() != null && !getActivity().getIntent().hasExtra(Constants.EXTRA_WALLET_ID)) {
            Analytics.getInstance(getActivity()).logSendFromLaunch();
        }
        return view;
    }

    public void setOnWalletClickListener(MyWalletsAdapter.OnWalletClickListener listener) {
        this.listener = listener;
    }

    public void setupAdapter() {
        recyclerView.setAdapter(new MyWalletsAdapter(wallet -> {
            if (listener != null) {
                listener.onWalletClick(wallet);
            } else if (getTargetFragment() != null) {
                getTargetFragment().onActivityResult(REQUEST_WALLET_ID, Activity.RESULT_OK,
                        new Intent().putExtra(Constants.EXTRA_WALLET_ID, wallet.getId()));
            }
            WalletChooserDialogFragment.this.dismiss();
        }, getAvailableWallets()));
    }

    public List<Wallet> getAvailableWallets() {
        List<Wallet> wallets = null;
        if (getTargetFragment() == null) {
            wallets = new ArrayList<>();
            for (Wallet walletRealmObject : RealmManager.getAssetsDao().getWallets()) {
                if (walletRealmObject.isMultisig()) {
                    continue;
                }
                if (walletRealmObject.isPayable() &&
                        Long.valueOf(walletRealmObject.getAvailableBalance()) > 150 &&
                        walletRealmObject.getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()) {
                    if (isMainNet) {
                        if (walletRealmObject.getNetworkId() == NativeDataHelper.NetworkId.MAIN_NET.getValue()) {
                            wallets.add(walletRealmObject);

                        }
                    } else {
                        wallets.add(walletRealmObject);
                    }
                }
            }
        } else if (getArguments() != null) {
            if (getArguments().getInt(ARG_NETWORK_ID, -1) == -1) {
                wallets = RealmManager.getAssetsDao().getWallets(getArguments().getInt(ARG_CURRENCY_ID), false);
            } else {
                wallets = RealmManager.getAssetsDao()
                        .getWallets(getArguments().getInt(ARG_CURRENCY_ID), getArguments().getInt(ARG_NETWORK_ID), false);
            }
        }

        if (exceptMultisig) {
            List<Wallet> result = new ArrayList<>();
            for (Wallet wallet : wallets) {
                if (wallet.getMultisigWallet() == null) {
                    result.add(wallet);
                }
            }

            return result;
        } else {
            return wallets;
        }
    }

    @OnClick(R.id.button_back)
    void onClickBack() {
        dismiss();
    }

    public boolean isMainNet() {
        return isMainNet;
    }

    public void setMainNet(boolean mainNet) {
        isMainNet = mainNet;
    }
}
