/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.MainActivity;
import io.multy.ui.fragments.send.SendSummaryFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;

public class CompleteDialogFragment extends DialogFragment {

    public static CompleteDialogFragment newInstance(int chainId) {
        CompleteDialogFragment completeDialogFragment = new CompleteDialogFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.CHAIN_ID, chainId);
        completeDialogFragment.setArguments(args);
        return completeDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (getTag() != null && getTag().equals(SendSummaryFragment.TAG_SEND_SUCCESS)) {
            if (getArguments() != null) {
                Analytics.getInstance(getActivity()).logSendSuccessLaunch(getArguments().getInt(Constants.CHAIN_ID));
            }
        }
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_complete, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.button_close)
    public void onClickClose() {
        if (getTag() != null && getTag().equals(SendSummaryFragment.TAG_SEND_SUCCESS)) {
            if (getArguments() != null) {
                Analytics.getInstance(getActivity()).logSendSuccess(AnalyticsConstants.BUTTON_CLOSE, getArguments().getInt(Constants.CHAIN_ID));
            }
        }
        startActivity(new Intent(getActivity(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        getActivity().finish();
    }
}
