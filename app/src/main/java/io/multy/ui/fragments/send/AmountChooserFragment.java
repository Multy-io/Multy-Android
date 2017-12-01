/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindDimen;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.AssetRequestViewModel;


public class AmountChooserFragment extends BaseFragment {

    public static AmountChooserFragment newInstance(){
        return new AmountChooserFragment();
    }

    @BindView(R.id.root)
    ConstraintLayout root;
    @BindView(R.id.group_send)
    Group groupSend;
    @BindView(R.id.input_balance_original)
    EditText inputOriginal;
    @BindView(R.id.input_balance_currency)
    EditText inputCurrency;
    @BindView(R.id.text_total)
    TextView total;

    @BindInt(R.integer.zero)
    int zero;
    @BindDimen(R.dimen.amount_size_huge)
    int sizeHuge;
    @BindDimen(R.dimen.amount_size_medium)
    int sizeMedium;

    private AssetRequestViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amount_chooser, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);
        return view;
    }

    @OnClick(R.id.button_next)
    void onClickNext(){
        if (!TextUtils.isEmpty(inputOriginal.getText())){
            viewModel.setAmount(Double.valueOf(inputOriginal.getText().toString()));
            ((AssetSendActivity) getActivity()).setFragment(R.string.ready_to_send, R.id.container, SendSummaryFragment.newInstance());
        } else {
            Toast.makeText(getActivity(), R.string.choose_amount, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.input_balance_original)
    void onClickBalanceOriginal(){
        inputOriginal.animate().scaleY(1.5f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        inputOriginal.animate().scaleX(1.5f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        inputCurrency.animate().scaleY(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        inputCurrency.animate().scaleX(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
    }

    @OnClick(R.id.input_balance_currency)
    void onClickBalanceCurrency(){
        inputOriginal.animate().scaleY(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        inputOriginal.animate().scaleX(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        inputCurrency.animate().scaleY(1.5f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        inputCurrency.animate().scaleX(1.5f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
    }

}

