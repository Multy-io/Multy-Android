/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.seed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.fragments.BaseSeedFragment;

public class SeedResultFragment extends BaseSeedFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_seed_result, container, false);
        ButterKnife.bind(this, convertView);

        return convertView;
    }

    public void onClickNext() {
//        getActivity().getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.container, )
    }
}
