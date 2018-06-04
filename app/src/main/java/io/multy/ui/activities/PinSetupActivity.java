/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.storage.SecurePreferencesHelper;
import io.multy.ui.adapters.PinDotsAdapter;
import io.multy.util.Constants;
import io.multy.util.PinEditText;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;

public class PinSetupActivity extends BaseActivity {

    private final static int STEP_ENTER = 0;
    private final static int STEP_REPEAT = 1;

    @BindView(R.id.text_pin)
    TextView textViewTitle;

    @BindView(R.id.recycler_view_dots)
    RecyclerView recyclerViewIndicator;

    @BindView(R.id.edit_pin)
    PinEditText inputPin;

    private LinearLayoutManager dotsLayoutManager;
    private String pin;
    private int step = STEP_ENTER;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pin_create);
        ButterKnife.bind(this);

        dotsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        init();
    }

    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }

    private void init() {
        recyclerViewIndicator.setAdapter(getDotsGreyAdapter());
        recyclerViewIndicator.setLayoutManager(dotsLayoutManager);
        recyclerViewIndicator.setHasFixedSize(true);

        inputPin.requestFocus();
        if (Prefs.getBoolean(Constants.PREF_LOCK)) {
            inputPin.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }

                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }, 100);
        }
        inputPin.setKeyImeChangeListener((keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
            }
        });
        inputPin.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    && inputPin.getText().length() < PinDotsAdapter.COUNT) {
                int position = inputPin.getText().length();
                ImageView dot = (ImageView) dotsLayoutManager.getChildAt(position);
                dot.setImageResource(R.drawable.circle_border_grey);
            }
            return false;
        });
        inputPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    return;
                }
                ImageView dot = (ImageView) dotsLayoutManager.getChildAt(editable.length() - 1);
                if (dot != null) {
                    dot.setImageResource(R.drawable.circle_blue);
                }


                if (editable.toString().length() == 6) {
                    if (step == STEP_ENTER) {
                        pin = editable.toString();
                        step = STEP_REPEAT;
                        textViewTitle.setText(R.string.repeat_pin);
                        inputPin.setText("");
                        recyclerViewIndicator.setAdapter(getDotsGreyAdapter());
                    } else {
                        if (pin.equals(editable.toString())) {
                            Analytics.getInstance(PinSetupActivity.this).logSettings(AnalyticsConstants.ENTRANCE_SETTINGS_PIN_SET);
                            SecurePreferencesHelper.putString(getApplicationContext(), Constants.PREF_PIN, pin);
                            Prefs.putBoolean(Constants.PREF_LOCK, true);
                            Toast.makeText(PinSetupActivity.this, R.string.success, Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(PinSetupActivity.this, R.string.pins_does_not_match, Toast.LENGTH_LONG).show();
                            clear();
                        }
                    }
                }
            }
        });
    }

    private PinDotsAdapter getDotsGreyAdapter() {
        PinDotsAdapter adapter = new PinDotsAdapter();
        adapter.setupDotsAsGray();
        return adapter;
    }

    private void clear() {
        recyclerViewIndicator.setAdapter(getDotsGreyAdapter());
        step = STEP_ENTER;
        pin = "";
        textViewTitle.setText(R.string.set_your_pin_code);
        inputPin.setText("");
    }

    @OnClick(R.id.button_back)
    void onClickBack(View view) {
        view.setEnabled(false);
        onBackPressed();
    }
}
