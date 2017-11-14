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
import io.multy.viewmodels.SeedViewModel;

public class SeedFragment extends BaseSeedFragment {

    private SeedViewModel seedModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_seed, container, false);
        ButterKnife.bind(this, convertView);
        initViewModel();
        subscribeViewModel();

        return convertView;
    }

    private void initViewModel() {
//        final String mnemonic = NativeDataHelper.makeMnemonic();
//        final byte[] seed = NativeDataHelper.makeSeed(mnemonic);
//
//        seedModel = ViewModelProviders.of(getActivity()).get(SeedViewModel.class);
//        seedModel.getPosition().setValue(0);
//        seedModel.getBinarySeed().setValue(seed);
//        seedModel.getPhrase().setValue(mnemonic.split(" "));
    }

    private void subscribeViewModel() {
//        seedModel.getPosition().observe(this, integer -> {
//            int value = integer ++;
//            textViewCount.setText((String.valueOf(value)) + " of " + seedModel.getPhrase().getValue().length);
//            textViewPhrase.setText(seedModel.getPhrase().getValue()[integer]);
//        });
    }

    //    @OnClick(R.id.button_next)
    public void onClickNext() {
//        final int position = seedModel.getPosition().getValue();
//        if (position == seedModel.getPhrase().getValue().length){
//            showNext(new SeedSummaryFragment());
//        } else {
//            seedModel.getPosition().setValue(position + 1);
//        }
    }
}
