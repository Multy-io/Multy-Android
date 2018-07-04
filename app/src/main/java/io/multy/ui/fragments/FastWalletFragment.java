/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;

public class FastWalletFragment extends Fragment {

    @BindView(R.id.text_balance_original)
    TextView textBalance;
    @BindView(R.id.text_balance_currency)
    TextView textBalanceFiat;
    @BindView(R.id.text_name)
    TextView walletName;
    @BindView(R.id.image_logo)
    ImageView imageLogo;

    @BindView(R.id.root)
    View rootView;

    private Wallet wallet;
    private View.OnTouchListener listener;
    private long walletId = -1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup convertView = (ViewGroup) inflater.inflate(R.layout.fragment_fast_wallet, container, false);
        ButterKnife.bind(this, convertView);

        if (walletId != -1) {
            setupInfo();
        }

        return convertView;
    }

    private void setupInfo() {
        if (wallet == null || !wallet.isValid()) {
            wallet = RealmManager.getAssetsDao().getWalletById(walletId);
        }

        walletName.setText(wallet.getWalletName());
        textBalance.setText(wallet.getBalanceLabel());
        textBalanceFiat.setText(wallet.getFiatBalanceLabel());
        imageLogo.setImageResource(wallet.getIconResourceId());

        rootView.setOnClickListener(v -> {
            v.setTag(walletId);
            rootView.setOnTouchListener(listener);
        });
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public long getWalletId() {
        return walletId;
    }

    public void setListener(View.OnTouchListener listener) {
        this.listener = listener;
    }

    public void hideRight() {
        rootView.animate().translationXBy(rootView.getWidth()).setDuration(100).start();
    }

    public void hideLeft() {
        rootView.animate().translationXBy(-rootView.getWidth()).setDuration(100).start();
    }

    public void showRight() {
        rootView.animate().translationXBy(-rootView.getWidth()).setDuration(100).start();
    }

    public void showLeft() {
        rootView.animate().translationXBy(rootView.getWidth()).setDuration(100).start();
    }
}
