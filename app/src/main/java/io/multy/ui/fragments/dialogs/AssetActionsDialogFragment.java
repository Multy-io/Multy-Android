/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.CreateAssetActivity;
import io.multy.util.Constants;

/**
 * Created by anschutz1927@gmail.com on 23.02.18.
 */

public class AssetActionsDialogFragment extends BottomSheetDialogFragment implements DialogInterface.OnShowListener {

    public static final String TAG = AssetActionsDialogFragment.class.getSimpleName();

    private Listener listener;

    public static AssetActionsDialogFragment getInstance() {
        return new AssetActionsDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ActionsBottomSheetDialog);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.bottom_sheet_wallet_action, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);
        dialog.setOnShowListener(this);
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

    public void setListener (Listener listener) {
        this.listener = listener;
    }

    @OnClick(R.id.button_create)
    public void onClickCreate(View v) {
        if (getActivity() != null) {
            v.setEnabled(false);
            dismiss();
            getActivity().startActivityForResult(new Intent(getActivity(), CreateAssetActivity.class)
                    .addCategory(Constants.EXTRA_RESTORE), Constants.REQUEST_CODE_CREATE);
        }
    }

    @OnClick(R.id.button_create_multisig)
    public void onClickCreateMultisig(View view) {
        view.setEnabled(false);
        dismiss();
        startActivity(new Intent(view.getContext(), CreateAssetActivity.class)
                .putExtra(CreateAssetActivity.EXTRA_MULTISIG, CreateAssetActivity.EXTRA_MULTISIG_CREATE));
    }

    @OnClick(R.id.button_join_multisig)
    public void onJoinMultisig(View view) {
        view.setEnabled(false);
        dismiss();
        startActivity(new Intent(view.getContext(), CreateAssetActivity.class)
                .putExtra(CreateAssetActivity.EXTRA_MULTISIG, CreateAssetActivity.EXTRA_MULTISIG_JOIN));
    }

    @OnClick(R.id.button_import)
    public void onClickImport(View v) {
        v.setEnabled(false);
        v.postDelayed(() -> v.setEnabled(true), 500);
        if (getActivity() != null) {
            DonateDialog.getInstance(Constants.DONATE_ADDING_IMPORT_WALLET).show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
        }
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
