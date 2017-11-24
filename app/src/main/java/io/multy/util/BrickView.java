package io.multy.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import io.multy.R;

public class BrickView extends FrameLayout {

    public static final int ANIMATION_DURATION = 400;

    private View fillView;
    private boolean isGreen = false;

    public BrickView(@NonNull Context context) {
        super(context);
        init();
    }

    public BrickView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View rootView = inflate(getContext(), R.layout.brick_view, this);
        fillView = rootView.findViewById(R.id.fill_view);
        setBrickBackground(false);
    }

    private void setBrickBackground(boolean isPending) {
        if (isPending) {
            setBackgroundResource(getStrokedShape());
        } else {
            setBackgroundResource(R.drawable.brick_gray);
        }
    }

    private int getStrokedShape() {
        if (isGreen) {
            return R.drawable.brick_gray_stroked_green;
        } else {
            return R.drawable.brick_gray_stroked_blue;
        }
    }

    public void setBrickBackground(int resId) {
        setBackgroundResource(resId);
    }

    public void enableGreenMode() {
        isGreen = true;
        fillView.setBackgroundResource(R.drawable.brick_green);
    }

    public void makeFull() {
        int cx = 0;
        int cy = 0;
        int finalRadius = Math.max(fillView.getWidth(), fillView.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(fillView, cx, cy, 0, finalRadius);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        fillView.setVisibility(View.VISIBLE);
        anim.start();
    }

    private void hideFull() {
        int cx = (fillView.getLeft() + fillView.getRight()) / 2;
        int cy = (fillView.getTop() + fillView.getBottom()) / 2;
        int initialRadius = fillView.getWidth();
        Animator anim = ViewAnimationUtils.createCircularReveal(fillView, cx, cy, initialRadius, 0);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fillView.setVisibility(View.INVISIBLE);
            }
        });
        anim.start();
    }

    public void makePending() {
        setBrickBackground(true);
        hideFull();

    }

    public void makeEmpty() {
        setBrickBackground(false);
        hideFull();
    }
}
