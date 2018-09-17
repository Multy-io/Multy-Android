/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import io.multy.util.Constants;

/**
 * Created by anschutz1927@gmail.com on 03.03.18.
 */

public class ChainChooserFragment extends BaseChooserFragment implements ChainAdapter.OnItemClickListener {

    public static final String TAG = ChainChooserFragment.class.getSimpleName();
    private static final String EXTRA_IS_MULTISIG = "EXTRA_IS_MULTISIG";

    @BindArray(R.array.available_chain_image_ids)
    TypedArray availableChainImageIds;
    @BindArray(R.array.available_chain_abbrev)
    String[] availableChainAbbrevs;
    @BindArray(R.array.available_chain_name)
    String[] availableChainNames;
    @BindArray(R.array.available_chain_net_types)
    int[] availableChainNets;
    @BindArray(R.array.available_chain_ids)
    int[] availableChainIds;
    @BindArray(R.array.soon_chain_image_ids)
    TypedArray disabledChainImageIds;
    @BindArray(R.array.soon_chain_abbrev)
    String[] disabledChainSoonAbbrevs;
    @BindArray(R.array.soon_chain_name)
    String[] disabledChainNames;
    @BindArray(R.array.soon_chain_donate_addresses)
    TypedArray disabledChainDonationCodes;
    @BindArray(R.array.available_chain_multisig_image_ids)
    TypedArray availableMultisigChainImageIds;
    @BindArray(R.array.available_multisig_chain_abbrev)
    String[] availableMultisigChainAbbrevs;
    @BindArray(R.array.available_multisig_chain_name)
    String[] availableMultisigChainNames;
    @BindArray(R.array.available_multisig_chain_net_types)
    int[] availableMultisigChainNets;
    @BindArray(R.array.available_multisig_chain_ids)
    int[] availableMultisigChainIds;

    private int chainNet;
    private String chainCurrency;

    public static ChainChooserFragment getInstance() {
        return new ChainChooserFragment();
    }

    public static ChainChooserFragment getInstance(boolean isMultisig) {
        ChainChooserFragment fragment = getInstance();
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_MULTISIG, isMultisig);
        fragment.setArguments(args);
        return fragment;
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
    public void onClickAvailableChain(String clickedChainName, int clickedChainNet, int clickedChainId) {
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            Intent data = new Intent();
            data.putExtra(Constants.CHAIN_NAME, clickedChainName);
            data.putExtra(Constants.CHAIN_NET, clickedChainNet);
            data.putExtra(Constants.CHAIN_ID, clickedChainId);
            targetFragment.onActivityResult(Constants.REQUEST_CODE_SET_CHAIN, Activity.RESULT_OK, data);
        }
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
        if (getArguments() != null && getArguments().getBoolean(EXTRA_IS_MULTISIG)) {
            chainAvailableAdapter.setAvailableChainsData(chainNet, chainCurrency, availableMultisigChainImageIds,
                    availableMultisigChainAbbrevs, availableMultisigChainNames, availableMultisigChainNets, availableMultisigChainIds);
            setSoonGroupVisibility(View.GONE);
        } else {
            chainAvailableAdapter.setAvailableChainsData(chainNet, chainCurrency, availableChainImageIds,
                    availableChainAbbrevs, availableChainNames, availableChainNets, availableChainIds);
            chainSoonAdapter.setSoonChainsData(disabledChainImageIds, disabledChainSoonAbbrevs,
                    disabledChainNames, disabledChainDonationCodes);
        }
    }

    public void setSelectedChain(String chainCurrency, int chainNet) {
        this.chainCurrency = chainCurrency;
        this.chainNet = chainNet;
    }
}
