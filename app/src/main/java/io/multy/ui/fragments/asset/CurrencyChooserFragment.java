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
import io.multy.ui.adapters.CurrencyConvertAdapter;
import io.multy.ui.fragments.BaseChooserFragment;
import io.multy.ui.fragments.dialogs.DonateDialog;

/**
 * Created by anschutz1927@gmail.com on 03.03.18.
 */

public class CurrencyChooserFragment extends BaseChooserFragment implements CurrencyConvertAdapter.OnItemClickListener {

    public static final String TAG = CurrencyChooserFragment.class.getSimpleName();

    @BindArray(R.array.available_currencies_to_convert_image_ids)
    TypedArray availableCurrencyImageIds;
    @BindArray(R.array.available_currencies_to_convert_abbrev)
    String[] availableCurrencyAbbrevs;
    @BindArray(R.array.available_currencies_to_convert_name)
    String[] availableCurrencyNames;
    @BindArray(R.array.soon_currencies_to_convert_image_ids)
    TypedArray disabledCurrencyImageIds;
    @BindArray(R.array.soon_currencies_to_convert_abbrev)
    String[] disabledCurrencyAbbrevs;
    @BindArray(R.array.soon_currencies_to_convert_name)
    String[] disabledCurrencyNames;
    @BindArray(R.array.soon_currencies_to_convert_donate_addresses)
    TypedArray disabledCurrencyDonationCodes;

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
        setContainerVisibility(View.VISIBLE);
        return v;
    }

    @Override
    public void onDestroyView() {
        setContainerVisibility(View.GONE);
        super.onDestroyView();
    }

    @Override
    public void onClickAvailableCurrency(String clickedDisabledCurrencyName) {
        //todo handle switch chains
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onClickDisabledCurrency(String clickedDisabledCurrencyName, int donationCode) {
        if (getActivity() != null) {
            DonateDialog.getInstance(donationCode).show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
        }
    }

    private void initialize(FragmentActivity activity) {
        super.setTitle(R.string.currency_to_convert);
        CurrencyConvertAdapter chainAvailableAdapter = new CurrencyConvertAdapter(CurrencyConvertAdapter.CurrencyType.AVAILABLE, this);
        CurrencyConvertAdapter chainSoonAdapter = new CurrencyConvertAdapter(CurrencyConvertAdapter.CurrencyType.SOON, this);
        getBlockAvailableRecyclerView().setLayoutManager(new LinearLayoutManager(activity));
        getBlockAvailableRecyclerView().setAdapter(chainAvailableAdapter);
        getBlockSoonRecyclerView().setLayoutManager(new LinearLayoutManager(activity));
        getBlockSoonRecyclerView().setAdapter(chainSoonAdapter);
        chainAvailableAdapter.setAvailableCurrenciesData(availableCurrencyAbbrevs[0], availableCurrencyImageIds, availableCurrencyAbbrevs, availableCurrencyNames);
        chainSoonAdapter.setSoonCurrenciesData(disabledCurrencyImageIds, disabledCurrencyAbbrevs, disabledCurrencyNames, disabledCurrencyDonationCodes);
    }

    private void setContainerVisibility(int visibility) {
        View fullContainer = getActivity().findViewById(R.id.full_container);
        if (fullContainer != null) {
            fullContainer.setVisibility(visibility);
        }
    }
}
