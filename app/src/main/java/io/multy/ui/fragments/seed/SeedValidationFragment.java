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

import java.util.ArrayList;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.UserId;
import io.multy.model.responses.AuthResponse;
import io.multy.storage.RealmManager;
import io.multy.storage.SettingsDao;
import io.multy.ui.fragments.BaseSeedFragment;
import io.multy.util.BrickView;
import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.viewmodels.SeedViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeedValidationFragment extends BaseSeedFragment {

    private static final long SEED_WORD_DURATION = 250;

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

    @BindArray(R.array.seed_words)
    String[] seedWords;

    private SeedViewModel seedModel;
    private StringBuilder phrase = new StringBuilder();
    private int count = 1;
    private int maxCount = 0;
    private Handler handler = new Handler();
    private boolean isProceedRunning = false;
    private String currentSeedWord;

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
                    buttonNext.setText(R.string.next_word);
                    return;
                } else if (editable.toString().length() == 1) {
                    inputWord.setGravity(Gravity.CENTER_HORIZONTAL);
                }
                if (editable.length() > 0) {
                    ArrayList<String> suggestions = new ArrayList<>();
                    boolean isFullCoincidence = false;
                    for (String s : seedWords) {
                        if (s.startsWith(editable.toString())) {
                            suggestions.add(s);
                            if (s.equals(editable.toString())) {
                                isFullCoincidence = true;
                            }
                        }
                    }
                    currentSeedWord = null;
                    if (suggestions.size() == 1) {
                        buttonNext.setText(suggestions.get(0));
                        currentSeedWord = suggestions.get(0);
                    } else if (suggestions.size() > 1) {
                        buttonNext.setText(editable);
                        if (isFullCoincidence) {
                            buttonNext.append(getString(R.string._or_) + editable);
                            currentSeedWord = inputWord.getText().toString();
                        }
                        buttonNext.append(getString(R.string.tree_dots));
                    } else {
                        inputWord.setText(editable.subSequence(0, editable.length() - 1));
                        inputWord.setSelection(inputWord.getText().toString().length());
                    }
                } else {
                    buttonNext.setText(R.string.next_word);
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
        if (isProceedRunning || currentSeedWord == null || currentSeedWord.isEmpty()) {
            return;
        }
        isProceedRunning = true;
        phrase.append(currentSeedWord);
        inputWord.setText(currentSeedWord);
        inputWord.setSelection(inputWord.getText().toString().length());
        handler.postDelayed(() -> {
            if (count == maxCount) {
                inputWord.animate().alpha(0).setDuration(BrickView.ANIMATION_DURATION / 2).start();
            }
            inputWord.setText("");
            currentSeedWord = null;
            redrawOne(false);
            buttonNext.setEnabled(false);
            if (count == maxCount) {
                if (getActivity().getIntent().hasCategory(Constants.EXTRA_RESTORE)) {
                    restore(phrase.toString(), getActivity(), () -> {
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
            isProceedRunning = false;
            handler.postDelayed(() -> buttonNext.setEnabled(true), BrickView.ANIMATION_DURATION);
        }, SEED_WORD_DURATION);
    }

    public void restore(String phrase, Context context, Runnable callback) {
        try {
            seedModel.isLoading.setValue(true);

            byte[] seed = NativeDataHelper.makeSeed(phrase);
            final String userId = NativeDataHelper.makeAccountId(seed);
            MultyApi.INSTANCE.auth(userId).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful()) {
                        Multy.makeInitialized();
                        SettingsDao settingsDao = RealmManager.getSettingsDao();
                        settingsDao.setUserId(new UserId(userId));
                        settingsDao.setByteSeed(new ByteSeed(seed));
                        Prefs.putString(Constants.PREF_AUTH, response.body().getToken());
                        seedModel.isLoading.setValue(false);
                        seedModel.failed.setValue(false);
                        callback.run();
                    } else {
                        onSeedRestoreFailure(callback);
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    onSeedRestoreFailure(callback);
                }
            });
        } catch (JniException e) {
            e.printStackTrace();
            onSeedRestoreFailure(callback);
        }
    }

    private void onSeedRestoreFailure(Runnable callback) {
        seedModel.isLoading.setValue(false);
        seedModel.failed.setValue(true);
        callback.run();
    }

    private void clear() {
        RealmManager.removeDatabase(getActivity());
        Prefs.clear();
    }
}
