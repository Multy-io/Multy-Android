/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments;

import android.support.v4.app.Fragment;

import io.multy.R;


public class BaseSeedFragment extends Fragment {

    public void subscribeSeedLiveData() {

    }

    public void showNext(Fragment fragment) {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }

    public void repeat() {

    }

    public void cancel() {

    }
}
