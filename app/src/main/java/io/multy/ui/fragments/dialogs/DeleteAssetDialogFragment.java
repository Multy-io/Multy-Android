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
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;


public class DeleteAssetDialogFragment extends DialogFragment {

    public static final String TAG = DeleteAssetDialogFragment.class.getSimpleName();

    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.text_message)
    TextView textMessage;
    @BindView(R.id.button_positive)
    TextView buttonPositive;

    private Listener listener;

    public static DeleteAssetDialogFragment getInstance(Listener listener) {
        DeleteAssetDialogFragment fragment = new DeleteAssetDialogFragment();
        fragment.setListener(listener);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_settings_reset, container, false);
        ButterKnife.bind(this, view);
        textTitle.setText(R.string.delete_wallet);
        textMessage.setText(R.string.delete_confirm);
        buttonPositive.setText(R.string.yes);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void setListener(Listener listener) {
        this.listener = listener;
    }

    @OnClick(R.id.button_positive)
    public void onClickPositive() {
        dismiss();
        if (listener != null) {
            listener.onClickConfirm();
        }
    }

    @OnClick(R.id.button_neutral)
    public void onClickNeutral() {
        dismiss();
    }

    public interface Listener {
        void onClickConfirm();
    }
}
