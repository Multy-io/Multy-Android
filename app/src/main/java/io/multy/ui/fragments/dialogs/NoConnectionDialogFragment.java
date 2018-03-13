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
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.util.NetworkUtils;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;

public class NoConnectionDialogFragment extends DialogFragment {

    private final AtomicBoolean isShowing = new AtomicBoolean(false);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_no_internet, container, false);
        ButterKnife.bind(this, view);
        Analytics.getInstance(getActivity()).logNoInternetLaunch();
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

    @OnClick(R.id.button_try_again)
    public void onClickCheckConnection() {
        Analytics.getInstance(getActivity()).logNoInternet(AnalyticsConstants.NO_INTERNET_CHECK);
        if (NetworkUtils.isConnected(getContext())) {
            dismiss();
            isShowing.set(false);
        } else {
            SimpleDialogFragment dialog = SimpleDialogFragment.newInstanceNegative(R.string.check_internet_connection,
                    R.string.no_connection, null);
            dialog.show(getFragmentManager(), "");
            dialog.setTitleSize(18);
        }
    }

    public AtomicBoolean isShowing() {
        return isShowing;
    }
}
