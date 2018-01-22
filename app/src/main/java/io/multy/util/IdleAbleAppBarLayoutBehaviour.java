/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.support.design.widget.AppBarLayout;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Workaround for appbar layout bug. Non touchable recyclerview after scrolling
 */
public class IdleAbleAppBarLayoutBehaviour extends AppBarLayout.Behavior {

    public IdleAbleAppBarLayoutBehaviour() {
        super();
    }

    public IdleAbleAppBarLayoutBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        checkNestedScroll(dyUnconsumed, child, target, type);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        checkNestedScroll(dy, child, target, type);
    }

    private void checkNestedScroll(int dy, AppBarLayout child, View target, int type) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {

            final int currentOffset = getTopAndBottomOffset();
            if ((dy < 0 && currentOffset == 0)
                    || (dy > 0 && currentOffset == -child.getTotalScrollRange())) {
                ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
            }
        }
    }
}