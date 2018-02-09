/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by neterebsky on 09.12.15.
 */
public class OnSlideTouchListener implements View.OnTouchListener {
    private float x1, x2;
    private float y1, y2;

    private int SLIDE_MIN_THRESHOLD;

    public OnSlideTouchListener() {
        SLIDE_MIN_THRESHOLD = 100;
    }

    /**
     * @param SLIDE_MIN_THRESHOLD minimum threshold to activate action (100 as default)
     */
    public OnSlideTouchListener(int SLIDE_MIN_THRESHOLD) {
        this.SLIDE_MIN_THRESHOLD = SLIDE_MIN_THRESHOLD;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            // when user first touches the screen we get x and y coordinate
            case MotionEvent.ACTION_DOWN: {
                x1 = event.getX();
                y1 = event.getY();
                return true; // it has to be TRUE to make ACTION_UP activated
            }
            case MotionEvent.ACTION_UP: {
                x2 = event.getX();
                y2 = event.getY();
                float deltaY = y2 - y1;
                float deltaX = x2 - x1;

                // if delta less then SLIDE_MIN_THRESHOLD, then it is click
                if (Math.abs(deltaX) < SLIDE_MIN_THRESHOLD && Math.abs(deltaY) < SLIDE_MIN_THRESHOLD) {
                    return true;
                } else { // if delta more then SLIDE_MIN_THRESHOLD, then it is slide
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        if (deltaX > 0) {
                            return onSlideRight();
                        }
                    }
                }
            }
        }

        return true;
    }

    public boolean onSlideRight() {
        return false;
    }
}