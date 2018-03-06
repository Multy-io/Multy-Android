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

public class CurrencyChooserFragment extends BaseChooserFragment implements ChainAdapter.OnClickListener {

    public static final String TAG = CurrencyChooserFragment.class.getSimpleName();

    @BindArray(R.array.currencies_to_convert_available_image_ids)
    TypedArray currenciesAvailableImageIds;
    @BindArray(R.array.currencies_to_convert_available_abbrev)
    String[] currenciesAvailableAbbrev;
    @BindArray(R.array.currencies_to_convert_available_name)
    String[] currenciesAvailableName;
    @BindArray(R.array.currencies_to_convert_soon_image_ids)
    TypedArray currenciesSoonImageIds;
    @BindArray(R.array.currencies_to_convert_soon_abbrev)
    String[] currenciesSoonAbbrev;
    @BindArray(R.array.currencies_to_convert_soon_name)
    String[] currensiesSoonName;

    public static CurrencyChooserFragment getInstance() {
        return new CurrencyChooserFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        super.setTitle(R.string.currency_to_convert);
        ChainAdapter chainAvailableAdapter = new ChainAdapter(ChainAdapter.ChainType.AVAILABLE, this);
        ChainAdapter chainSoonAdapter = new ChainAdapter(ChainAdapter.ChainType.SOON, this);
        getBlockAvailableRecyclerView().setLayoutManager(new LinearLayoutManager(activity));
        getBlockAvailableRecyclerView().setAdapter(chainAvailableAdapter);
        getBlockSoonRecyclerView().setLayoutManager(new LinearLayoutManager(activity));
        getBlockSoonRecyclerView().setAdapter(chainSoonAdapter);
        chainAvailableAdapter.setAvailableChainsData(currenciesAvailableAbbrev[0], currenciesAvailableImageIds, currenciesAvailableAbbrev, currenciesAvailableName);
        chainSoonAdapter.setSoonChainsData(currenciesSoonImageIds, currenciesSoonAbbrev, currensiesSoonName);
    }
}
