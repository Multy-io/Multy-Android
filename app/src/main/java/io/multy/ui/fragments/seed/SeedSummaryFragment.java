/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.seed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseSeedFragment;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.util.analytics.Analytics;

/**
 * Created by andre on 08.11.2017.
 */

public class SeedSummaryFragment extends BaseSeedFragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_seed_summary, container, false);
        ButterKnife.bind(this, convertView);
        initBricks(recyclerView);
        setBrickColor(BRICK_BLUE);
        return convertView;
    }

    @OnClick(R.id.button_next)
    public void onClickNext() {
        showNext(new SeedValidationFragment());
    }

    @OnClick(R.id.button_repeat)
    public void onClickRepeat() {
        Analytics.getInstance(getActivity()).logSeedPhraseRepeat();
        SimpleDialogFragment.newInstance(
                R.string.repeat,
                R.string.repeat_message,
                view -> repeat()).show(getFragmentManager(), "");
    }
}
