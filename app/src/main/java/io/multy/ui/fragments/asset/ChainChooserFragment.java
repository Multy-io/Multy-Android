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

public class ChainChooserFragment extends BaseChooserFragment implements ChainAdapter.OnClickListener {

    public static final String TAG = ChainChooserFragment.class.getSimpleName();

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
    public void onClickSoonChain(String clickedChainName) {
        if (getActivity() != null) {
            DonateDialog.getInstance().show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
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
        chainAvailableAdapter.setAvailableChainsData(chainCurrency, chainsAvailableImageIds, chainsAvailableAbbrev, chainsAvailableName);
        chainSoonAdapter.setSoonChainsData(chainsSoonImageIds, chainsSoonAbbrev, chainsSoonName);
    }

    public void setSelectedChain(String chainCurrency) {
        this.chainCurrency = chainCurrency;
    }
}
