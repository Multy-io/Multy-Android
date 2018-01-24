/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.adapters.PinDotsAdapter;
import io.multy.ui.adapters.PinNumbersAdapter;
import io.multy.util.Constants;

public class PinSetupActivity extends AppCompatActivity implements PinNumbersAdapter.OnFingerPrintClickListener, PinNumbersAdapter.OnNumberClickListener, PinNumbersAdapter.OnBackSpaceClickListener {

    private final static int STEP_ENTER = 0;
    private final static int STEP_REPEAT = 1;

    @BindView(R.id.text_title)
    TextView textViewTitle;

    @BindView(R.id.recycler_view_dots)
    RecyclerView recyclerViewIndicator;

    @BindView(R.id.recycler_view_numbers)
    RecyclerView recyclerViewKeyboard;

    private LinearLayoutManager dotsLayoutManager;
    private String pin;
    private StringBuilder stringBuilder = new StringBuilder();
    private int step = STEP_ENTER;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pin);
        ButterKnife.bind(this);

        dotsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        init();
    }

    private void init() {
        recyclerViewIndicator.setAdapter(new PinDotsAdapter());
        recyclerViewIndicator.setLayoutManager(dotsLayoutManager);
        recyclerViewIndicator.setHasFixedSize(true);

        recyclerViewKeyboard.setAdapter(new PinNumbersAdapter(this, this, this, false));
        recyclerViewKeyboard.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewKeyboard.setHasFixedSize(true);
    }

    private void clear() {
        init();
        step = STEP_ENTER;
        pin = "";
        stringBuilder = new StringBuilder();
        textViewTitle.setText(R.string.enter_new_pin);
    }

    @Override
    public void onFingerprintClick() {

    }

    @Override
    public void onNumberClick(int number) {
        ImageView dot = (ImageView) dotsLayoutManager.getChildAt(stringBuilder.toString().length());
        dot.setBackgroundResource(R.drawable.circle_white);
        stringBuilder.append(String.valueOf(number));

        if (stringBuilder.toString().length() == 6) {
            if (step == STEP_ENTER) {
                init();
                pin = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                step = STEP_REPEAT;
                textViewTitle.setText(R.string.repeat_pin);
            } else {
                if (pin.equals(stringBuilder.toString())) {
                    Prefs.putString(Constants.PREF_PIN, pin);
                    Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Entered pins does not match", Toast.LENGTH_LONG).show();
                    clear();
                }
            }
        }
    }

    @Override
    public void onBackSpaceClick() {
        if (stringBuilder.length() > 0) {
            ImageView dot = (ImageView) dotsLayoutManager.getChildAt(stringBuilder.toString().length() - 1);
            dot.setBackgroundResource(R.drawable.circle_border_white);
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
    }
}
