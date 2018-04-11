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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.DonationActivity;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;

/**
 * Created by anschutz1927@gmail.com on 05.03.18.
 */

public class DonateDialog extends DialogFragment {

    public static final String TAG = DonateDialog.class.getSimpleName();

    public static DonateDialog getInstance(int donationCode) {
        DonateDialog dialog = new DonateDialog();
        Bundle args = new Bundle();
        args.putInt(Constants.EXTRA_DONATION_CODE, donationCode);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_donate_notification, container, false);
        ButterKnife.bind(this, v);
        Analytics.getInstance(getContext()).logDonationAllertLaunch(getArguments() == null ?
                0 : getArguments().getInt(Constants.EXTRA_DONATION_CODE, 0));
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @OnClick(R.id.button_positive)
    void onPositiveClick(View v) {
        v.setEnabled(false);
        int donationCode = getArguments() == null ? 0 : getArguments().getInt(Constants.EXTRA_DONATION_CODE, 0);
        DonationActivity.showDonation(getContext(), donationCode);
        Analytics.getInstance(v.getContext()).logDonationAllertDonateClick(getArguments() == null ?
                0 : getArguments().getInt(Constants.EXTRA_DONATION_CODE, 0));
        dismiss();
    }
    @OnClick(R.id.button_negative)
    void onNegativeClick(View v) {
        v.setEnabled(false);
        Analytics.getInstance(v.getContext()).logDonationAllertClose(getArguments() == null ?
                0 : getArguments().getInt(Constants.EXTRA_DONATION_CODE, 0));
        dismiss();
    }
}
