/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindArray;
import io.multy.R;
import io.multy.ui.adapters.ExchangeAdapter;
import io.multy.ui.fragments.BaseChooserFragment;
import io.multy.ui.fragments.dialogs.DonateDialog;

/**
 * Created by anschutz1927@gmail.com on 05.03.18.
 */

public class ExchangeChooserFragment extends BaseChooserFragment implements ExchangeAdapter.OnItemClickListener {

    public static final String TAG = ExchangeChooserFragment.class.getSimpleName();

    @BindArray(R.array.available_exchange_name)
    String[] exchangeAvailableNames;
    @BindArray(R.array.soon_exchange_name)
    String[] exchangeSoonName;
    @BindArray(R.array.soon_exchange_donate_addresses)
    TypedArray disabledExchangeDonationCodes;

    public static ExchangeChooserFragment getInstance() {
        return new ExchangeChooserFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        initialize(getActivity());
        return v;
    }

    @Override
    public void onAvailableExchangeClick(String clickedExchangeName) {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onDisabledExchangeClick(String clickedExchangeName, int donationCode) {
        if (getActivity() != null) {
            DonateDialog.getInstance(donationCode).show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
        }
    }

    private void initialize(FragmentActivity activity) {
        super.setTitle(R.string.exchange);
        ExchangeAdapter availableAdapter = new ExchangeAdapter(ExchangeAdapter.ExchangeType.AVAILABLE, this);
        availableAdapter.setAvailableData(exchangeAvailableNames[0], exchangeAvailableNames);
        getBlockAvailableRecyclerView().setLayoutManager(new LinearLayoutManager(activity));
        getBlockAvailableRecyclerView().setAdapter(availableAdapter);
        ExchangeAdapter soonAdapter = new ExchangeAdapter(ExchangeAdapter.ExchangeType.SOON, this);
        soonAdapter.setSoonData(exchangeSoonName, disabledExchangeDonationCodes);
        getBlockSoonRecyclerView().setLayoutManager(new LinearLayoutManager(activity));
        getBlockSoonRecyclerView().setAdapter(soonAdapter);
    }
}
