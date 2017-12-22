/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.PinFragment;
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        viewModel = ViewModelProviders.of(this).get(ContactsViewModel.class);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.image_address)
    public void onClickAddresses() {
        new PinFragment().show(getFragmentManager(), "");
    }
}
