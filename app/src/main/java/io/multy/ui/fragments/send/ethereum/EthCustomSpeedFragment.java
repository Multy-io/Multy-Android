/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send.ethereum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;

/**
 * Created by anschutz1927@gmail.com on 21.03.18.
 */

public class EthCustomSpeedFragment extends BaseFragment {

    public static final String TAG = EthCustomSpeedFragment.class.getSimpleName();

    @BindView(R.id.text_price_value)
    EditText inputPrice;
    @BindView(R.id.text_limit_value)
    EditText inputLimit;

    public static EthCustomSpeedFragment getInstance() {
        return new EthCustomSpeedFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eth_custom_speed, container, false);
        ButterKnife.bind(this, view);
        inputPrice.addTextChangedListener(getTextWatcher(inputPrice));
        inputLimit.addTextChangedListener(getTextWatcher(inputLimit));
        return view;
    }

    @Override
    public void onDestroyView() {
        //save result here
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            Intent data = new Intent();
            data.putExtra(Constants.GAS_PRICE, inputPrice.getText().toString());
            data.putExtra(Constants.GAS_LIMIT, inputLimit.getText().toString());
            targetFragment.onActivityResult(Constants.REQUEST_CODE_SET_GAS, Activity.RESULT_OK, data);
        }
        super.onDestroyView();
    }

    private TextWatcher getTextWatcher(EditText editText) {
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                if (!TextUtils.isEmpty(input)) {
                    input = new StringBuilder(input.replace(" ", "")).reverse().toString();
                    input = input.replaceAll("...(?!$)", "$0 ");
                    input = new StringBuilder(input).reverse().toString();
                    editText.removeTextChangedListener(this);
                    editText.setText(input);
                    editText.addTextChangedListener(this);
                    editText.setSelection(editText.length());
                }
            }
        };
    }

    @OnTouch({R.id.text_price_value, R.id.text_limit_value})
    boolean onTouchInputs(View view, MotionEvent motionEvent) {
        if (view instanceof EditText) {
            view.postDelayed(() -> ((EditText) view).setSelection(((EditText) view).getText().length()), 100);
            view.requestFocus();
        }
        return false;
    }
}
