/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.DonateDialog;
import io.multy.util.analytics.Analytics;
import io.multy.viewmodels.ContactsViewModel;

/**
 * Created by Ihar Paliashchuk on 02.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class ContactsFragment extends BaseFragment {

    private ContactsViewModel viewModel;

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        viewModel = ViewModelProviders.of(this).get(ContactsViewModel.class);
        ButterKnife.bind(this, view);
        Analytics.getInstance(getActivity()).logContactsLaunch();
        return view;
    }

    @OnClick(R.id.card_donation)
    void onCardClick(View v) {
        v.setEnabled(false);
        v.postDelayed(() -> v.setEnabled(true), 500);
        if (getActivity() != null) {
            DonateDialog.getInstance().show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
        }
    }
}
