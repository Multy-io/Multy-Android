/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

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
import io.multy.ui.adapters.ChainAdapter;
import io.multy.ui.fragments.BaseChooserFragment;
import io.multy.ui.fragments.dialogs.DonateDialog;

/**
 * Created by anschutz1927@gmail.com on 03.03.18.
 */

public class ChainChooserFragment extends BaseChooserFragment implements ChainAdapter.OnItemClickListener {

    public static final String TAG = ChainChooserFragment.class.getSimpleName();

    @BindArray(R.array.available_chain_image_ids)
    TypedArray availableChainImageIds;
    @BindArray(R.array.available_chain_abbrev)
    String[] availableChainAbbrevs;
    @BindArray(R.array.available_chain_name)
    String[] availableChainNames;
    @BindArray(R.array.soon_chain_image_ids)
    TypedArray disabledChainImageIds;
    @BindArray(R.array.soon_chain_abbrev)
    String[] disabledChainSoonAbbrevs;
    @BindArray(R.array.soon_chain_name)
    String[] disabledChainNames;
    @BindArray(R.array.soon_chain_donate_addresses)
    TypedArray disabledChainDonationCodes;

    private String chainCurrency;

    public static ChainChooserFragment getInstance() {
        return new ChainChooserFragment();
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
    public void onClickAvailableChain(String clickedChainName) {
        //todo handle switch chains
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onClickSoonChain(String clickedChainName, int donationCode) {
        if (getActivity() != null) {
            DonateDialog.getInstance(donationCode).show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
        }
    }

    private void initialize(FragmentActivity activity) {
        super.setTitle(R.string.chain);
        ChainAdapter chainAvailableAdapter = new ChainAdapter(ChainAdapter.ChainType.AVAILABLE, this);
        ChainAdapter chainSoonAdapter = new ChainAdapter(ChainAdapter.ChainType.SOON, this);
        getBlockAvailableRecyclerView().setLayoutManager(new LinearLayoutManager(activity));
        getBlockAvailableRecyclerView().setAdapter(chainAvailableAdapter);
        getBlockSoonRecyclerView().setLayoutManager(new LinearLayoutManager(activity));
        getBlockSoonRecyclerView().setAdapter(chainSoonAdapter);
        chainAvailableAdapter.setAvailableChainsData(chainCurrency, availableChainImageIds, availableChainAbbrevs, availableChainNames);
        chainSoonAdapter.setSoonChainsData(disabledChainImageIds, disabledChainSoonAbbrevs, disabledChainNames, disabledChainDonationCodes);
    }

    public void setSelectedChain(String chainCurrency) {
        this.chainCurrency = chainCurrency;
    }
}
