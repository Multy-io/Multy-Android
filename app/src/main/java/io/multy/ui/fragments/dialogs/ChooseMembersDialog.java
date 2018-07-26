/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Activity;
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

import com.aigestudio.wheelpicker.widgets.WheelYearPicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.util.Constants;

/**
 * Created by anschutz1927@gmail.com on 20.07.18.
 */
public class ChooseMembersDialog extends BottomSheetDialogFragment implements DialogInterface.OnShowListener {

    public static final String TAG = ChooseMembersDialog.class.getSimpleName();
    public static final String EXTRA_MEMBERS = "EXTRA_MEMBERS";
    public static final String EXTRA_CONFIRMS = "EXTRA_CONFIRMS";
    public static final int REQUEST_MEMBERS = 1927;
    private static final String ARG_MEMBERS = "ARG_MEMBERS";
    private static final String ARG_CONFIRMS = "ARG_CONFIRMS";
    private static final int MIN_MEMBERS_COUNT = 2;
    private static final int MIN_CONFIRMS_COUNT = 1;

    @BindView(R.id.picker_members)
    WheelYearPicker pickerMembers;
    @BindView(R.id.picker_signs)
    WheelYearPicker pickerSigns;

    public static ChooseMembersDialog getInstance(int currentConfirms, int currentMembers) {
        ChooseMembersDialog dialog = new ChooseMembersDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_CONFIRMS, currentConfirms < MIN_CONFIRMS_COUNT ? 1 : currentConfirms);
        args.putInt(ARG_MEMBERS, currentMembers < MIN_MEMBERS_COUNT ? 2 : currentMembers);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ActionsBottomSheetDialogDark);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.bottom_sheet_choose_members, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);
        dialog.setOnShowListener(this);
        initialize();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        FrameLayout bottomSheet = ((BottomSheetDialog) dialog)
                .findViewById(android.support.design.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheet.setBackground(null);
        }
    }

    private void initialize() {
        pickerMembers.setYearStart(MIN_MEMBERS_COUNT);
        pickerMembers.setYearEnd(Constants.MULTISIG_MEMBERS_COUNT);
        pickerMembers.setSelectedYear(getArguments() != null ? getArguments().getInt(ARG_MEMBERS, MIN_MEMBERS_COUNT) : MIN_MEMBERS_COUNT);
        pickerMembers.setOnItemSelectedListener((picker, data, position) -> pickerSigns.setYearEnd((Integer) data));
        pickerSigns.setYearStart(MIN_CONFIRMS_COUNT);
        pickerSigns.setYearEnd(pickerMembers.getSelectedYear());
        pickerSigns.setSelectedYear(getArguments() != null ? getArguments().getInt(ARG_CONFIRMS, MIN_CONFIRMS_COUNT) : MIN_CONFIRMS_COUNT);
    }

    @OnClick(R.id.button_ok)
    void onClickOk(View view) {
        if (getDialog() != null && getTargetFragment() != null) {
            view.setEnabled(false);
            getTargetFragment().onActivityResult(REQUEST_MEMBERS, Activity.RESULT_OK,
                    new Intent().putExtra(EXTRA_MEMBERS, pickerMembers.getCurrentYear())
                            .putExtra(EXTRA_CONFIRMS, pickerSigns.getCurrentYear()));
            getDialog().dismiss();
        }
    }

    @OnClick(R.id.button_cancel)
    void onClickCancel() {
        if (getDialog() != null) {
            getDialog().dismiss();
        }
    }
}
