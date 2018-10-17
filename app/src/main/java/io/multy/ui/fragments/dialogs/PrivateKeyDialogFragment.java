/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.storage.RealmManager;
import io.multy.util.NativeDataHelper;
import io.multy.viewmodels.WalletViewModel;

/**
 * Created by anschutz1927@gmail.com on 23.02.18.
 */

public class PrivateKeyDialogFragment extends BottomSheetDialogFragment implements DialogInterface.OnShowListener {

    @BindView(R.id.text_key)
    TextView textKey;
    @BindView(R.id.text_currency)
    TextView textCurrency;

    private int currencyId;
    private int networkId;
    private WalletAddress address;
    private WalletViewModel viewModel;
    private Listener listener;

    public static PrivateKeyDialogFragment getInstance(WalletAddress address, int currencyId, int networkId) {
        PrivateKeyDialogFragment fragment = new PrivateKeyDialogFragment();
        fragment.setParams(address, currencyId, networkId);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.KeyBottomSheetDialog);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.bottom_sheet_private_key, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);
        dialog.setOnShowListener(this);
        viewModel = ViewModelProviders.of(requireActivity()).get(WalletViewModel.class);
        String key = getPrivateKey();
        if (key != null && !key.isEmpty()) {
            textKey.setText(getPrivateKey());
//            final String currency = String.format(getString(R.string.address_formatted),
//                            viewModel.getWalletLive().getValue().getCurrencyName());
//            textCurrency.setText(currency);
        } else {
            dismiss();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (listener != null) {
            listener.onDismiss();
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
        FrameLayout bottomSheet = d.findViewById(android.support.design.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheet.setBackground(null);
        }
    }

    private void setParams(WalletAddress address, int currencyId, int networkId) {
        this.address = address;
        this.currencyId = currencyId;
        this.networkId = networkId;
    }

    private String getPrivateKey() {
        try {
            if (viewModel.getWalletLive().getValue().shouldUseExternalKey()) {
                return RealmManager.getAssetsDao().getPrivateKey(address.getAddress(), currencyId, networkId).getPrivateKey();
            } else {
                byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();
                int walletIndex = viewModel.getWalletLive().getValue().getIndex();
                int addressIndex = address.getIndex();
                return NativeDataHelper.getMyPrivateKey(seed, walletIndex, addressIndex, currencyId, networkId);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            //TODO create new error message
//            Toast.makeText(Multy.getContext(), "Error while build private key", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void setListener (Listener listener) {
        this.listener = listener;
    }

    @OnClick(R.id.button_copy)
    public void onClickCopy(View view) {
        view.setEnabled(false);
        String key = getPrivateKey();
        if (key != null && !key.isEmpty()) {
            viewModel.copyToClipboard(getActivity(), getPrivateKey());
        }
        dismiss();
    }

    @OnClick(R.id.button_share)
    public void onClickShare(View view) {
        view.setEnabled(false);
        String key = getPrivateKey();
        if (key != null && !key.isEmpty()) {
            viewModel.share(getActivity(), getPrivateKey());
        }
        dismiss();
    }

    @OnClick(R.id.button_cancel)
    public void onClickCancel(View view) {
        view.setEnabled(false);
        dismiss();
    }

    public interface Listener {
        void onDismiss();
    }
}
