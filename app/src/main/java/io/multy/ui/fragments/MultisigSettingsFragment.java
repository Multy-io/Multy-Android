/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.ui.activities.CreateMultiSigActivity;

public class MultisigSettingsFragment extends BaseFragment {

    @BindView(R.id.text_signs)
    TextView textSigngs;
    @BindView(R.id.edit_name)
    TextInputEditText editName;
    @BindView(R.id.image_wallet)
    ImageView imageWallet;
    @BindView(R.id.text_wallet_name)
    TextView textWalletName;
    @BindView(R.id.text_wallet_address)
    TextView textWalletAddress;
    @BindView(R.id.button_delete)
    View buttonDelete;

    private Wallet wallet;
    private Wallet connectedWallet;

    public static MultisigSettingsFragment newInstance(Wallet wallet, Wallet connectedWallet) {
        MultisigSettingsFragment fragment = new MultisigSettingsFragment();
        fragment.setWallet(wallet);
        fragment.setConnectedWallet(connectedWallet);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_multisig_settings, container, false);
        ButterKnife.bind(this, convertView);
        fillViews();
        return convertView;
    }

    private void fillViews() {
        if (wallet != null && wallet.isValid() && connectedWallet != null && connectedWallet.isValid()) {
            editName.setText(wallet.getWalletName());
            textWalletAddress.setText(connectedWallet.getActiveAddress().getAddress());
            textWalletName.setText(connectedWallet.getWalletName());
            imageWallet.setImageResource(connectedWallet.getIconResourceId());
            textSigngs.setText(wallet.getMultisigWallet().getOwners().size() + " / " + wallet.getMultisigWallet().getOwnersCount());
            if (wallet.getMultisigWallet().getDeployStatus() > 2) {
                buttonDelete.setEnabled(false);
            }
        }
    }

    @OnClick(R.id.button_save)
    public void onClickSave() {
        hideFragment();
    }

    @OnClick(R.id.button_delete)
    public void onClickDelete() {
        showDeletePrompt();
    }

    public void showDeletePrompt() {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.delete_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    ((CreateMultiSigActivity) getActivity()).removeWallet();
                    dialog.cancel();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    dialog.cancel();
                    hideFragment();
                }).show();
    }

    private void hideFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public void setConnectedWallet(Wallet connectedWallet) {
        this.connectedWallet = connectedWallet;
    }

    @OnClick(R.id.button_cancel)
    void onClickCancel(View view) {
        if (getActivity() != null) {
            view.setEnabled(false);
            getActivity().onBackPressed();
        }
    }
}
