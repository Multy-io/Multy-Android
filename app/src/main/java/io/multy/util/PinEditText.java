/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by anschutz1927@gmail.com on 25.01.18.
 */

public class PinEditText extends android.support.v7.widget.AppCompatEditText {

    private KeyImeChange keyImeChangeListener;

    public PinEditText(Context context) {
        super(context);
    }

    public PinEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setKeyImeChangeListener(KeyImeChange listener) {
        keyImeChangeListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyImeChangeListener != null) {
            keyImeChangeListener.onKeyIme(keyCode, event);
        }
        return false;
    }

    public interface KeyImeChange {
        void onKeyIme(int keyCode, KeyEvent event);
    }
}
