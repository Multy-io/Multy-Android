/*
 * Copyright 2017 Idealnaya rabota LLC
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
import io.multy.R;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.WalletsAdapter;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;

public class WalletChooserDialogFragment extends DialogFragment {

    public static WalletChooserDialogFragment newInstance() {
        WalletChooserDialogFragment walletChooserDialogFragment = new WalletChooserDialogFragment();
        return new WalletChooserDialogFragment();
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private WalletsAdapter.OnWalletClickListener listener;

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
        View view = inflater.inflate(R.layout.fragment_wallet_chooser, container, false);
        ButterKnife.bind(this, view);
        setupAdapter();
        if (getActivity() != null && !getActivity().getIntent().hasExtra(Constants.EXTRA_WALLET_ID)) {
            Analytics.getInstance(getActivity()).logSendFromLaunch();
        }
        return view;
    }

    public void setOnWalletClickListener(WalletsAdapter.OnWalletClickListener listener) {
        this.listener = listener;
    }

    public void setupAdapter() {
        recyclerView.setAdapter(new WalletsAdapter(wallet -> {
            listener.onWalletClick(wallet);
            dismiss();
        }, getAvailableWallets()));
    }

    public ArrayList<WalletRealmObject> getAvailableWallets() {
        ArrayList<WalletRealmObject> wallets = new ArrayList<>();
        for (WalletRealmObject walletRealmObject : RealmManager.getAssetsDao().getWallets()) {
            if (walletRealmObject.getAvailableBalance() > 150) {
                wallets.add(walletRealmObject);
            }
        }

        return wallets;
    }
}
