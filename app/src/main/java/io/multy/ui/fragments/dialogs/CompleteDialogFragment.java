/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.MainActivity;
import io.multy.ui.fragments.DonationFragment;
import io.multy.ui.fragments.send.SendSummaryFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;

import static android.view.View.GONE;

public class CompleteDialogFragment extends DialogFragment {

    public static final String TAG = CompleteDialogFragment.class.getSimpleName();
    private final static String EXTRA_AMOUNT_COMPLETE_DIALOG = "amount_complete_dialog";

    @BindView(R.id.text_amount)
    TextView textAmount;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.text_name)
    TextView textName;
    @BindView(R.id.group_amount)
    Group groupAmount;

    public static CompleteDialogFragment newInstance(int chainId) {
        CompleteDialogFragment completeDialogFragment = new CompleteDialogFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.CHAIN_ID, chainId);
        args.putBoolean(EXTRA_AMOUNT_COMPLETE_DIALOG, false);
        completeDialogFragment.setArguments(args);
        return completeDialogFragment;
    }

    /**
     * @param chainId - Blockchain
     * @param amount - String amount with blockchain label ("like 0.1 BTC")
     * @param address - Strung address "to"
     * @return - {@link CompleteDialogFragment} instance
     */
    public static CompleteDialogFragment newInstance(int chainId, String amount, String address) {
        CompleteDialogFragment completeDialogFragment = new CompleteDialogFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.CHAIN_ID, chainId);
        args.putBoolean(EXTRA_AMOUNT_COMPLETE_DIALOG, true);
        args.putString(Constants.EXTRA_ADDRESS, address);
        args.putString(Constants.EXTRA_AMOUNT, amount);
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
        } else if (getTag() != null && getTag().equals(DonationFragment.TAG_SEND_SUCCESS)) {
            if (getArguments() != null) {
                Analytics.getInstance(getContext()).logDonationSuccessLaunch(getArguments().getInt(Constants.FEATURE_ID));
            }
        }
        return dialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        Log.d(TAG, "COMPLETE DIALOG MUST BE SHOWN");
        try {
            super.show(manager, tag);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
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
        if (getArguments() != null && getArguments().getBoolean(EXTRA_AMOUNT_COMPLETE_DIALOG)) {
            String address = getArguments().getString(Constants.EXTRA_ADDRESS);
            textAddress.setText(address);
            textAmount.setText(getArguments().getString(Constants.EXTRA_AMOUNT));
            String name = RealmManager.getSettingsDao().getContactNameOrNull(address);
            if (name != null) {
                textName.setVisibility(View.VISIBLE);
                textName.setText(name);
            }
        } else {
            groupAmount.setVisibility(GONE);
        }
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
