/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.adapters.CurrenciesAdapter;
import io.multy.util.CurrencyType;
import io.multy.viewmodels.WalletViewModel;

public class ListDialogFragment extends DialogFragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.text_title)
    TextView textView;

    public interface OnCurrencyClickListener {
        void onClickCurrency(String currency, int position);
    }

    private ArrayList<String> items;
    private CurrencyType currencyType;

    public static ListDialogFragment newInstance(ArrayList<String> items, CurrencyType currencyType) {
        ListDialogFragment listDialogFragment = new ListDialogFragment();
        listDialogFragment.setItems(items);
        listDialogFragment.setCurrencyType(currencyType);
        return listDialogFragment;
    }

    @NonNull
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_blue_list, container, false);
        ButterKnife.bind(this, view);
        textView.setText(currencyType == CurrencyType.CHAIN ? R.string.select_chain : R.string.select_currency);
        if (items != null && items.size() > 0) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(new CurrenciesAdapter(items, (currency, position) -> {
                WalletViewModel walletModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
                if (currencyType == CurrencyType.CHAIN) {
                    walletModel.chainCurrency.setValue(currency);
                } else {
                    walletModel.fiatCurrency.setValue(currency);
                }
                dismiss();
            }));
        } else {
            throw new IllegalArgumentException("Did u forgot to initialize items?");
        }
        return view;
    }

    @OnClick(R.id.button_cancel)
    public void onClickCancel() {
        dismiss();
    }

    public void setItems(ArrayList<String> items) {
        this.items = items;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }
}
