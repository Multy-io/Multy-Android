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
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import io.multy.R;
import io.multy.ui.fragments.BaseSeedFragment;
import io.multy.util.BrickView;
import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        seedModel = ViewModelProviders.of(getActivity()).get(SeedViewModel.class);
        setBaseViewModel(seedModel);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_seed_validation, container, false);
        ButterKnife.bind(this, convertView);

        seedModel = ViewModelProviders.of(getActivity()).get(SeedViewModel.class);
        if (!getActivity().getIntent().hasCategory(Constants.EXTRA_RESTORE)) {
            maxCount = seedModel.phrase.getValue().size() * 3;
        }

        init();
        return convertView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity().getIntent().hasCategory(Constants.EXTRA_RESTORE)) {
            maxCount = 15;
            count = 1;
            phrase.setLength(0);
        }
    }

    private void init(){
        initBricks(recyclerView);
        adapter.enableGreenMode();
        buttonNext.setText(R.string.next_word);
        setRedrawPosition(0);
        recyclerView.post(() -> redrawOne(true));
        inputWord.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    }

    private void refreshCounter() {
        textViewCounter.setText(count + " of " + maxCount);
    }

    @OnEditorAction(R.id.input_word)
    public boolean onEditorAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
            proceedNext();
            return true;
        }

        return false;
    }

    @OnClick(R.id.button_next)
    public void onClickNext() {
        proceedNext();
    }

    private void proceedNext() {
        if (inputWord.getText().toString().equals("")) {
            return;
        }
        phrase.append(inputWord.getText().toString());
        inputWord.setText("");
        redrawOne(false);
        refreshCounter();
        buttonNext.setEnabled(false);
        if (count == maxCount) {
            if (getActivity().getIntent().hasCategory(Constants.EXTRA_RESTORE)) {
                seedModel.restore(phrase.toString(), getActivity(), () -> showNext(new SeedResultFragment()));
            } else {
                boolean result = phrase.toString().equals(TextUtils.join(" ", seedModel.phrase.getValue()).replace("\n", " "));
                seedModel.failed.setValue(!result);
                showNext(new SeedResultFragment());
            }
        } else if (count != maxCount - 1) {
            redrawOne(true);
        }
        phrase.append(" ");
        count++;
        handler.postDelayed(() -> buttonNext.setEnabled(true), BrickView.ANIMATION_DURATION);
    }

}
