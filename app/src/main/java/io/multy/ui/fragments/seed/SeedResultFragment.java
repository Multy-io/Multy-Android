/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.seed;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.Multy;
import io.multy.R;
import io.multy.ui.fragments.BaseSeedFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.SeedViewModel;

public class SeedResultFragment extends BaseSeedFragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.button_next)
    TextView buttonNext;

    @BindView(R.id.image_result)
    ImageView imageViewResult;

    @BindView(R.id.text_title)
    TextView textViewTitle;

    @BindView(R.id.button_cancel)
    View buttonCancel;

    private SeedViewModel seedModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_seed_result, container, false);
        ButterKnife.bind(this, convertView);

        seedModel = ViewModelProviders.of(getActivity()).get(SeedViewModel.class);
        initBricks(recyclerView);
        showResult();
        return convertView;
    }

    private void showResult() {
        if (isFailure()) {
            setBrickColor(BRICK_RED);
            buttonNext.setText(R.string.try_again);
            imageViewResult.setImageResource(R.drawable.ic_fail);
            textViewTitle.setText(R.string.seed_result_fail);
        } else {
            if (getActivity().getIntent().hasCategory(Constants.EXTRA_RESTORE)) {
                textViewTitle.setText(R.string.seed_congrats_restore);
                if (!Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
                    Multy.makeInitialized();
                }
            }

            setBrickColor(BRICK_GREEN);
            buttonCancel.setVisibility(View.GONE);
            buttonNext.setText(R.string.great);
        }
    }

    private boolean isFailure() {
        return seedModel.failed.getValue() == null || seedModel.failed.getValue();
    }

    @OnClick(R.id.button_next)
    public void onClickNext() {
        if (isFailure()) {
            repeat();
        } else {
            close();
        }
    }
}
