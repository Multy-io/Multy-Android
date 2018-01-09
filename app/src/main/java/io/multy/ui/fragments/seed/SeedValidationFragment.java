/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.seed;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import io.multy.R;
import io.multy.ui.fragments.BaseSeedFragment;
import io.multy.util.BrickView;
import io.multy.util.Constants;
import io.multy.viewmodels.SeedViewModel;

public class SeedValidationFragment extends BaseSeedFragment {

    @BindView(R.id.input_word)
    EditText inputWord;

    @BindView(R.id.button_next)
    TextView buttonNext;

    @BindView(R.id.text_counter)
    TextView textViewCounter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.text_title)
    TextView textViewTitle;

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
            refreshCounter();
        } else {
            maxCount = 15;
            count = 1;
            phrase.setLength(0);
            textViewTitle.setText(R.string.restore_multy);
            refreshCounter();

            inputWord.requestFocus();
            inputWord.postDelayed(this::showKeyboard, 300);
        }

        init();
        return convertView;
    }

    private void showKeyboard() {
        InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(inputWord, InputMethodManager.SHOW_IMPLICIT);
    }

    private void init() {
        initBricks(recyclerView);
        adapter.enableGreenMode();
        buttonNext.setText(R.string.next_word);
        setRedrawPosition(0);
        recyclerView.post(() -> redrawOne(true));
        inputWord.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        inputWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 0) {
                    inputWord.setGravity(Gravity.LEFT);
                } else if (editable.toString().length() == 1) {
                    inputWord.setGravity(Gravity.CENTER_HORIZONTAL);
                }
            }
        });
    }

    private void refreshCounter() {
        textViewCounter.setText(String.format("%d of %d", count, maxCount));
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
        if (inputWord.getText().toString().equals("") || inputWord.getText().toString().length() < 3) {
            return;
        }
        phrase.append(inputWord.getText().toString());
        if (count == maxCount) {
            inputWord.animate().alpha(0).setDuration(BrickView.ANIMATION_DURATION / 2).start();
        } else {
            inputWord.setText("");
        }
        redrawOne(false);
        buttonNext.setEnabled(false);
        if (count == maxCount) {
            if (getActivity().getIntent().hasCategory(Constants.EXTRA_RESTORE)) {
                seedModel.restore(phrase.toString(), getActivity(), () -> {
                    hideKeyboard(getActivity());
                    SeedValidationFragment.this.showNext(new SeedResultFragment());
                });
            } else {
                boolean result = phrase.toString().equals(TextUtils.join(" ", seedModel.phrase.getValue()).replace("\n", " "));
                Prefs.putBoolean(Constants.PREF_BACKUP_SEED, result);
                seedModel.failed.setValue(!result);
                showNext(new SeedResultFragment());
            }
        } else {
            redrawOne(true);
            phrase.append(" ");
            count++;
            refreshCounter();
        }
        handler.postDelayed(() -> buttonNext.setEnabled(true), BrickView.ANIMATION_DURATION);
    }

}
