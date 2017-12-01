/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.Wallet;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.adapters.FeeAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.AssetSendViewModel;

public class TransactionFeeFragment extends BaseFragment implements FeeAdapter.OnFeeClickListener{

    public static TransactionFeeFragment newInstance(){
        return new TransactionFeeFragment();
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.switcher)
    SwitchCompat switcher;
    @BindView(R.id.text_donation_allow)
    TextView textDonationAllow;
    @BindView(R.id.text_donation_summ)
    TextView textDonationSumm;
    @BindView(R.id.input_donation)
    EditText inputDonation;
    @BindView(R.id.text_fee_currency)
    TextView textFeeCurrency;
    @BindView(R.id.group_donation)
    Group groupDonation;

//    private FeeAdapter adapter;

    private AssetSendViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_fee, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        recyclerView.setAdapter(new FeeAdapter(getActivity(), this));
        setupSwitcher();
        return view;
    }

    @OnClick(R.id.button_next)
    void onClickNext(){
        ((AssetSendActivity) getActivity()).setFragment(R.string.send, R.id.container, AmountChooserFragment.newInstance());
    }

    @Override
    public void onFeeClick(Wallet fee) {
        viewModel.saveFee(fee);
    }

    private void setupSwitcher(){
        switcher.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                textDonationAllow.setBackground(getResources().getDrawable(R.drawable.shape_top_round_white, null));
                groupDonation.setVisibility(View.VISIBLE);
            } else {
                textDonationAllow.setBackground(getResources().getDrawable(R.drawable.shape_squircle_white, null));
                groupDonation.setVisibility(View.GONE);
            }
        });
    }
}
