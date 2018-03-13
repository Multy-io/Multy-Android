/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.seed;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseSeedFragment;
import io.multy.util.BrickView;
import io.multy.util.Constants;
import io.multy.viewmodels.SeedViewModel;

public class SeedFragment extends BaseSeedFragment {

    @BindView(R.id.text_switcher)
    TextSwitcher textSwitcher;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.button_next)
    TextView buttonNext;

    private SeedViewModel seedModel;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_seed, container, false);
        ButterKnife.bind(this, convertView);
        initSwitcher();
        initBricks(recyclerView);
        initViewModel();
        subscribeViewModel();
        setRedrawPosition(0);
        recyclerView.post(() -> redrawTriplet(true));
        buttonNext.setText(R.string.next);

        disableButton();
        showKeyboard(getActivity());
        return convertView;
    }

    private void disableButton() {
        buttonNext.setEnabled(false);
        handler.postDelayed(() -> buttonNext.setEnabled(true), BrickView.ANIMATION_DURATION);
    }

    private void initViewModel() {
        seedModel = ViewModelProviders.of(getActivity()).get(SeedViewModel.class);
        seedModel.initData();
//        Toast.makeText(getActivity(), "" + seedModel.phrase.getValue().size(), Toast.LENGTH_LONG).show();
    }

    private void subscribeViewModel() {
        seedModel.position.observe(this, integer -> textSwitcher.setText(seedModel.phrase.getValue().get(integer)));
        seedModel.position.setValue(0);
    }

    private void initSwitcher() {
        Animation inAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
        Animation outAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);
        textSwitcher.setInAnimation(inAnimation);
        textSwitcher.setOutAnimation(outAnimation);
        textSwitcher.setFactory(this::generateTextView);
    }

    private TextView generateTextView() {
        final float spacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24.0f, getResources().getDisplayMetrics());
        final float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20.0f, getResources().getDisplayMetrics());
        TextView textView = new TextView(getActivity());
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(textSize);
        textView.setLineSpacing(spacing, 1.0f);
        textView.setTextColor(getResources().getColor(R.color.gray_dark));
        return textView;
    }

    @OnClick(R.id.button_next)
    public void onClickNext() {
        final int nextPosition = seedModel.position.getValue() + 1;
        buttonNext.setEnabled(false);
        if (nextPosition == seedModel.phrase.getValue().size()) {
            redrawTriplet(false);
            if (getActivity() != null && getActivity().getIntent().getType() != null
                    && getActivity().getIntent().getType().equals(Constants.FLAG_VIEW_SEED_PHRASE)) {
                handler.postDelayed(() -> getActivity().finish(), BrickView.ANIMATION_DURATION);
            } else {
                handler.postDelayed(() -> showNext(new SeedSummaryFragment()), BrickView.ANIMATION_DURATION);
            }
        } else {
            if (nextPosition == seedModel.phrase.getValue().size() - 1) {
                buttonNext.setText(R.string.seed_continue);
            }
            redrawTriplet(false);
            seedModel.position.setValue(nextPosition);
            if (nextPosition != seedModel.phrase.getValue().size()) {
                redrawTriplet(true);
            }
            handler.postDelayed(() -> buttonNext.setEnabled(true), BrickView.ANIMATION_DURATION);
        }
    }
}
