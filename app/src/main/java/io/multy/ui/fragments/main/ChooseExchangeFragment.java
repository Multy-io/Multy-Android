/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main;

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
import io.multy.ui.fragments.BaseChooseFragment;
import io.multy.ui.fragments.dialogs.DonateThisDialog;

/**
 * Created by anschutz1927@gmail.com on 05.03.18.
 */

public class ChooseExchangeFragment extends BaseChooseFragment implements ExchangeAdapter.Listener {

    public static final String TAG = ChooseExchangeFragment.class.getSimpleName();

    @BindArray(R.array.exchange_available_name)
    String[] exchangeAvailableNames;
    @BindArray(R.array.exchange_soon_name)
    String[] exchangeSoonName;

    public static ChooseExchangeFragment getInstance() {
        return new ChooseExchangeFragment();
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
    public void onAvailableExchangeClick(String clickedChainName) {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onSoonExchangeClick(String clickedChainName) {
        if (getActivity() != null) {
            DonateThisDialog.getInstance().show(getActivity().getSupportFragmentManager(), DonateThisDialog.TAG);
        }
    }

    private void initialize(FragmentActivity activity) {
        super.setTitle(R.string.exchange);
        ExchangeAdapter availableAdapter = new ExchangeAdapter(ExchangeAdapter.ExchangeType.AVAILABLE, this);
        availableAdapter.setAvailableData(exchangeAvailableNames[0], exchangeAvailableNames);
        getRecyclerAvailable().setLayoutManager(new LinearLayoutManager(activity));
        getRecyclerAvailable().setAdapter(availableAdapter);
        ExchangeAdapter soonAdapter = new ExchangeAdapter(ExchangeAdapter.ExchangeType.SOON, this);
        soonAdapter.setSoonData(exchangeSoonName);
        getRecyclerSoon().setLayoutManager(new LinearLayoutManager(activity));
        getRecyclerSoon().setAdapter(soonAdapter);
    }
}
