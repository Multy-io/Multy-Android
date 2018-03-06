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
import io.multy.ui.fragments.BaseChooseFragment;
import io.multy.ui.fragments.dialogs.DonateThisDialog;

/**
 * Created by anschutz1927@gmail.com on 03.03.18.
 */

public class ChooseChainFragment extends BaseChooseFragment implements ChainAdapter.Listener {

    public static final String TAG = ChooseChainFragment.class.getSimpleName();

    @BindArray(R.array.chain_available_image_ids)
    TypedArray chainsAvailableImageIds;
    @BindArray(R.array.chain_available_abbrev)
    String[] chainsAvailableAbbrev;
    @BindArray(R.array.chain_available_name)
    String[] chainsAvailableName;
    @BindArray(R.array.chain_soon_image_ids)
    TypedArray chainsSoonImageIds;
    @BindArray(R.array.chain_soon_abbrev)
    String[] chainsSoonAbbrev;
    @BindArray(R.array.chain_soon_name)
    String[] chainsSoonName;


    private String chainCurrency;

    public static ChooseChainFragment getInstance() {
        return new ChooseChainFragment();
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
    public void onAvailableChainClick(String clickedChainName) {
        //todo handle switch chains
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onSoonChainClick(String clickedChainName) {
        if (getActivity() != null) {
            DonateThisDialog.getInstance().show(getActivity().getSupportFragmentManager(), DonateThisDialog.TAG);
        }
    }

    private void initialize(FragmentActivity activity) {
        super.setTitle(R.string.chain);
        ChainAdapter chainAvailableAdapter = new ChainAdapter(ChainAdapter.ChainType.AVAILABLE, this);
        ChainAdapter chainSoonAdapter = new ChainAdapter(ChainAdapter.ChainType.SOON, this);
        getRecyclerAvailable().setLayoutManager(new LinearLayoutManager(activity));
        getRecyclerAvailable().setAdapter(chainAvailableAdapter);
        getRecyclerSoon().setLayoutManager(new LinearLayoutManager(activity));
        getRecyclerSoon().setAdapter(chainSoonAdapter);
        chainAvailableAdapter.setAvailableChainsData(chainCurrency, chainsAvailableImageIds, chainsAvailableAbbrev, chainsAvailableName);
        chainSoonAdapter.setSoonChainsData(chainsSoonImageIds, chainsSoonAbbrev, chainsSoonName);
    }

    public void setSelectedChain(String chainCurrency) {
        this.chainCurrency = chainCurrency;
    }
}
