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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.AssetsDao;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.MyWalletsAdapter;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;

public class WalletSelectorDialog extends DialogFragment {

    private static final String ARG_NETWORK_ID = "ARG_NETWORK_ID";
    private static final String ARG_BLOCK_CHAIN_ID = "ARG_BLOCK_CHAIN_ID";
    private static final String ARG_MULTISIG = "ARG_MULTISIG";
    private static final String ARG_MAIN_NETWORK = "ARG_MAIN_NETWORK";
    private static final String ARG_AVAILABLE_WALLETS = "ARG_AVAILABLE_WALLETS";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private MyWalletsAdapter.OnWalletClickListener listener;

    public static WalletSelectorDialog newInstance(Bundle arguments, MyWalletsAdapter.OnWalletClickListener listener) {
        WalletSelectorDialog dialog = new WalletSelectorDialog();
        dialog.setArguments(arguments);
        dialog.setListener(listener);
        return dialog;
    }

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

    public void setListener(MyWalletsAdapter.OnWalletClickListener listener) {
        this.listener = listener;
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

        TextView textAvailable = view.findViewById(R.id.text_not_available);
        textAvailable.setVisibility(recyclerView.getAdapter().getItemCount() == 0 ? View.VISIBLE : View.GONE);

        return view;
    }

    public void setupAdapter() {
        AssetsDao assetsDao = RealmManager.getAssetsDao();
        List<Wallet> wallets;
        List<Wallet> result = new ArrayList<>();

        final int networkId = getArguments().getInt(ARG_NETWORK_ID);
        final int blockChainId = getArguments().getInt(ARG_BLOCK_CHAIN_ID);
        final boolean isMainNetworkOnly = getArguments().getBoolean(ARG_MAIN_NETWORK);
        final boolean exceptMultisig = getArguments().getBoolean(ARG_MULTISIG);
        final boolean onlyAvailableWallets = getArguments().getBoolean(ARG_AVAILABLE_WALLETS);

        if (blockChainId != -1) {
            wallets = networkId != -1 ? assetsDao.getWallets(blockChainId, networkId, exceptMultisig) : assetsDao.getWallets(blockChainId, exceptMultisig);
        } else {
            wallets = assetsDao.getWallets();
        }

        for (Wallet wallet : wallets) {
            if ((onlyAvailableWallets && wallet.isPayable() && Long.valueOf(wallet.getAvailableBalance()) > 150) || !onlyAvailableWallets) {
                if (isMainNetworkOnly && wallet.isMainNetwork() || !isMainNetworkOnly) {
                    result.add(wallet);
                }
            }
        }

        recyclerView.setAdapter(new MyWalletsAdapter(new MyWalletsAdapter.OnWalletClickListener() {
            @Override
            public void onWalletClick(Wallet wallet) {
                listener.onWalletClick(wallet);
                dismiss();
            }
        }, result));
    }

    @OnClick(R.id.button_back)
    void onClickBack() {
        dismiss();
    }

    public static class WalletSelectorDialogBuilder {
        private int blockChainId = -1;
        private int networkId = -1;
        private boolean isMainNetworkOnly = true;
        private boolean exceptMultisig = false;
        private boolean onlyPayableWallets = false;
        private MyWalletsAdapter.OnWalletClickListener listener;

        public WalletSelectorDialogBuilder setBlockChainId(int blockChainId) {
            this.blockChainId = blockChainId;
            return this;
        }

        public WalletSelectorDialogBuilder setNetworkId(int networkId) {
            this.networkId = networkId;
            return this;
        }

        public WalletSelectorDialogBuilder setIsMainNetworkOnly(boolean isMainNetworkOnly) {
            this.isMainNetworkOnly = isMainNetworkOnly;
            return this;
        }

        public WalletSelectorDialogBuilder setExceptMultisig(boolean exceptMultisig) {
            this.exceptMultisig = exceptMultisig;
            return this;
        }

        public WalletSelectorDialogBuilder setOnlyPayableWallets(boolean onlyPayableWallets) {
            this.onlyPayableWallets = onlyPayableWallets;
            return this;
        }

        public WalletSelectorDialogBuilder setListener(MyWalletsAdapter.OnWalletClickListener listener) {
            this.listener = listener;
            return this;
        }

        public WalletSelectorDialog create() {
            Bundle arguments = new Bundle();
            arguments.putInt(ARG_NETWORK_ID, networkId);
            arguments.putInt(ARG_BLOCK_CHAIN_ID, blockChainId);
            arguments.putBoolean(ARG_MAIN_NETWORK, isMainNetworkOnly);
            arguments.putBoolean(ARG_MULTISIG, exceptMultisig);
            arguments.putBoolean(ARG_AVAILABLE_WALLETS, onlyPayableWallets);
            return newInstance(arguments, listener);
        }
    }
}
