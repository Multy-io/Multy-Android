/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.adapters.PinDotsAdapter;
import io.multy.ui.adapters.PinNumbersAdapter;

public class PinFragment extends DialogFragment implements PinNumbersAdapter.OnFingerPrintClickListener, PinNumbersAdapter.OnNumberClickListener, View.OnClickListener {

    private static final int COUNT = 6;

    @BindView(R.id.recycler_view_dots)
    RecyclerView recyclerViewDots;
    @BindView(R.id.recycler_view_numbers)
    RecyclerView recyclerViewNumbers;

    private RecyclerView.LayoutManager dotsLayoutManager;
    private StringBuilder stringBuilder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_pin, container, false);
        ButterKnife.bind(this, convertView);

        dotsLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewDots.setAdapter(new PinDotsAdapter());
        recyclerViewDots.setLayoutManager(dotsLayoutManager);
        recyclerViewDots.setHasFixedSize(true);

        recyclerViewNumbers.setAdapter(new PinNumbersAdapter(this, this, this, true));
        recyclerViewNumbers.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerViewNumbers.setHasFixedSize(true);

        stringBuilder = new StringBuilder();
        return convertView;
    }

    @Override
    public void onFingerprintClick() {
        Toast.makeText(getActivity(), "fingerprint click", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNumberClick(int number) {
        stringBuilder.append(String.valueOf(number));

        ImageView dot = (ImageView) dotsLayoutManager.getChildAt(stringBuilder.toString().length() - 1);
        dot.setBackgroundResource(R.drawable.circle_white);

        if (stringBuilder.toString().length() == COUNT) {
            dismiss();
        }
    }

    @Override
    public void onClick(View view) {

    }
}
