/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.AssetSendViewModel;

public class SendSummaryFragment extends BaseFragment {

    public static SendSummaryFragment newInstance(){
        return new SendSummaryFragment();
    }

    @BindView(R.id.text_receiver_balance_original)
    TextView textReceiverBalanceOriginal;
    @BindView(R.id.text_receiver_balance_currency)
    TextView textReceiverBalanceCurrency;
    @BindView(R.id.text_receiver_address)
    TextView textReceiverAddress;
    @BindView(R.id.text_wallet_name)
    TextView textWalletName;
    @BindView(R.id.text_sender_balance_original)
    TextView textSenderBalanceOriginal;
    @BindView(R.id.text_sender_balance_currency)
    TextView textSenderBalanceCurrency;
    @BindView(R.id.text_fee_speed)
    TextView textFeeSpeed;
    @BindView(R.id.text_fee_amount)
    TextView textFeeAmount;

    private AssetSendViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_summary, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        setInfo();
        return view;
    }

    @OnClick(R.id.button_next)
    void onClickNext(){
        AssetSendDialogFragment dialog = new AssetSendDialogFragment();
        dialog.show(getActivity().getFragmentManager(), null);
    }

    private void setInfo(){
        textReceiverBalanceOriginal.setText(String.valueOf(viewModel.getAmount()));
        textReceiverBalanceCurrency.setText(String.valueOf(viewModel.getAmount()));
        textReceiverAddress.setText(viewModel.getReceiverAddress().getValue());
        textWalletName.setText(viewModel.getWallet().getName());
        textSenderBalanceOriginal.setText(viewModel.getWallet().getBalanceWithCode());
        textSenderBalanceCurrency.setText(viewModel.getWallet().getBalanceWithCode());
        textFeeSpeed.setText(viewModel.getFee().getTime());
        textFeeAmount.setText(String.valueOf(viewModel.getFee().getCost()));
    }

}
