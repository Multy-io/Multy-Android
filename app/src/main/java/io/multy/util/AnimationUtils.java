/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewAnimationUtils;

public class AnimationUtils {

    public static final int DURATION_MEDIUM = 500;

    public interface OnViewConcealListener {
        void onConcealed();
    }

    public static void createReveal(final View view, float cx, float cy, final int startColor, final int endColor) {
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                float finalRadius = Math.max(v.getWidth(), v.getHeight());
                makeReveal(v, 0, finalRadius, (int) cx, (int) cy).start();
                startColorAnimation(v, startColor, endColor, DURATION_MEDIUM + 100);
            }
        });
    }

    public static void createConceal(final View v, float cx, float cy, final int startColor, final int endColor, OnViewConcealListener listener) {
        float startRadius = Math.max(v.getWidth(), v.getHeight());
        Animator animator = makeReveal(v, startRadius, 0, (int) cx, (int) cy);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onConcealed();
                super.onAnimationEnd(animation);
            }
        });
        animator.start();
        startColorAnimation(v, startColor, endColor, DURATION_MEDIUM + 100);
    }

    private static Animator makeReveal(final View v, final float startRadius, final float finalRadius, final int cx, final int cy) {
        Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, startRadius, finalRadius);
        anim.setDuration(DURATION_MEDIUM);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        return anim;
    }

    private static void startColorAnimation(final View view, final int startColor, final int endColor, int duration) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(startColor, endColor);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(valueAnimator -> view.setBackgroundColor((Integer) valueAnimator.getAnimatedValue()));
        anim.setDuration(duration);
        anim.start();
    }
}
