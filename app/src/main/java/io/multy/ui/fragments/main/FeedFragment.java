/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.DonateDialog;
import io.multy.ui.fragments.dialogs.WebDialogFragment;
import io.multy.util.Constants;
import io.multy.util.RoundedCornersDrawable;
import io.multy.util.analytics.Analytics;
import io.multy.viewmodels.FeedViewModel;

public class FeedFragment extends BaseFragment {

    @BindView(R.id.image)
    ImageView imageViewChallenge;

    @BindView(R.id.button_challenge)
    CardView cardViewChallenge;

    private FeedViewModel viewModel;

    public static FeedFragment newInstance() {
        return new FeedFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(this).get(FeedViewModel.class);
        Analytics.getInstance(getActivity()).logActivityLaunch();
        setRoundedImage();
        return view;
    }

    private void setRoundedImage() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.activity_bgd);
        RoundedCornersDrawable round = new RoundedCornersDrawable(bitmap, getResources().getDimension(R.dimen.card_challenge_radius), 0);
        cardViewChallenge.setPreventCornerOverlap(false);
        imageViewChallenge.setBackground(round);
    }

    @OnClick(R.id.button_challenge)
    void onClickChallenge() {
        WebDialogFragment.newInstance("http://multy.io/index.php/feature-challenge/").show(getFragmentManager(), "");
    }

    @OnClick(R.id.card_donation)
    void onClickDonate(View view) {
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);
        DonateDialog.getInstance(Constants.DONATE_ADDING_ACTIVITY).show(getChildFragmentManager(), DonateDialog.TAG);
    }
}
