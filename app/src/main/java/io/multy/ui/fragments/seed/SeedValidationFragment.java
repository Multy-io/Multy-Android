/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.seed;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseSeedFragment;
import io.multy.util.BrickView;
import io.multy.viewmodels.SeedViewModel;

public class SeedValidationFragment extends BaseSeedFragment {

    @BindView(R.id.input_word)
    TextInputEditText inputWord;

    @BindView(R.id.button_next)
    TextView buttonNext;

    @BindView(R.id.text_counter)
    TextView textViewCounter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private SeedViewModel seedModel;
    private StringBuilder phrase = new StringBuilder();
    private int count = 1;
    private int maxCount = 0;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_seed_validation, container, false);
        ButterKnife.bind(this, convertView);

        seedModel = ViewModelProviders.of(getActivity()).get(SeedViewModel.class);
        maxCount = seedModel.phrase.getValue().size() * 3;
        initBricks(recyclerView);
        adapter.enableGreenMode();
        buttonNext.setText(R.string.next_word);
        setRedrawPosition(0);
        recyclerView.post(() -> redrawOne(true));
        return convertView;
    }

    private void refreshCounter() {
        textViewCounter.setText(count + " of " + maxCount);
    }

    @OnClick(R.id.button_next)
    public void onClickNext() {
        if (inputWord.getText().toString().equals("")) {
            return;
        }
        phrase.append(inputWord.getText().toString());
        inputWord.setText("");
        redrawOne(false);
        refreshCounter();
        buttonNext.setEnabled(false);
        if (count == maxCount) {
            boolean result = phrase.toString().equals(TextUtils.join("", seedModel.phrase.getValue()));
            seedModel.failed.setValue(!result);
            showNext(new SeedResultFragment());
        } else if (count != maxCount - 1) {
            redrawOne(true);
        }
        count++;
        handler.postDelayed(() -> buttonNext.setEnabled(true), BrickView.ANIMATION_DURATION);
    }
}
