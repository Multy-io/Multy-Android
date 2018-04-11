/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
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

    public static WalletChooserDialogFragment newInstance() {
        WalletChooserDialogFragment walletChooserDialogFragment = new WalletChooserDialogFragment();
        return new WalletChooserDialogFragment();
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private MyWalletsAdapter.OnWalletClickListener listener;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
            listener.onWalletClick(wallet);
            WalletChooserDialogFragment.this.dismiss();
        }, getAvailableWallets()));
    }

    public ArrayList<Wallet> getAvailableWallets() {
        ArrayList<Wallet> wallets = new ArrayList<>();
        for (Wallet walletRealmObject : RealmManager.getAssetsDao().getWallets()) {
            if (Long.valueOf(walletRealmObject.getAvailableBalance()) > 150 &&
                    walletRealmObject.getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue() &&
                    walletRealmObject.getNetworkId() == NativeDataHelper.NetworkId.MAIN_NET.getValue()) {
                wallets.add(walletRealmObject);
            }
        }

        return wallets;
    }

    @OnClick(R.id.button_back)
    void onClickBack() {
        dismiss();
    }
}
