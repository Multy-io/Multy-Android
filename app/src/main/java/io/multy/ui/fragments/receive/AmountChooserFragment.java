/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.receive;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.AssetRequestViewModel;


public class AmountChooserFragment extends BaseFragment {

    public static AmountChooserFragment newInstance() {
        return new AmountChooserFragment();
    }

    @BindView(R.id.group_send)
    Group groupSend;
    @BindView(R.id.input_balance_original)
    EditText input;
    @BindInt(R.integer.zero)
    int zero;

    private AssetRequestViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amount_chooser, container, false);
        ButterKnife.bind(this, view);

        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);
        if (viewModel.getAmount() != zero) {
            input.setText(String.valueOf(viewModel.getAmount()));
        }
        groupSend.setVisibility(View.GONE);
        return view;
    }

    @OnClick(R.id.button_next)
    void onClickNext() {
        if (!TextUtils.isEmpty(input.getText())) {
            viewModel.setAmount(Double.valueOf(input.getText().toString()));
        }
        getFragmentManager().popBackStack();
    }

}
