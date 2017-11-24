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
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseSeedFragment;

public class HelloSeedFragment extends BaseSeedFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_seed_hello, container, false);
        ButterKnife.bind(this, convertView);

        return convertView;
    }

    @OnClick(R.id.button_next)
    public void onClickNext() {
        showNext(new SeedFragment(), SeedFragment.class.getSimpleName());
    }
}
