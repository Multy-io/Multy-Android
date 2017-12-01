/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;

/**
 * Created by anschutz1927@gmail.com on 23.11.17.
 */

public class WalletActionsDialog extends DialogFragment {

    public static final String TAG = WalletActionsDialog.class.getSimpleName();

    private WalletActionsDialog.Callback callback;

    public static WalletActionsDialog newInstance(WalletActionsDialog.Callback callback) {
        WalletActionsDialog dialog = new WalletActionsDialog();
        dialog.setCallback(callback);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater()
                .inflate(R.layout.view_assets_action, null);
        ButterKnife.bind(this, v);
        return new AlertDialog
//                .Builder(getContext(), R.style.FullScreenTheme)
                .Builder(getContext(), R.style.NoTitleTheme)
                .setView(v)
                .create();
    }

    private void setCallback(WalletActionsDialog.Callback callback) {
        this.callback = callback;
    }

    @OnClick(R.id.text_create)
    void onCardAddClick() {
        callback.onCardAddClick();
    }

    @OnClick(R.id.text_import)
    void onCardImportClick() {
        callback.onCardImportClick();
    }

    @OnClick(R.id.text_cancel)
    void onCardCancelClick() {
        this.callback = null;
        this.dismiss();
    }

    public interface Callback {
        void onCardAddClick();
        void onCardImportClick();
    }
}
