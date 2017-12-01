/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.util;


import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;

import timber.log.Timber;

public class SpannableUtil {

    /**
     * Applies ForegroundColorSpan, ClickableSpan and UnderlineSpan to given part of text
     *
     * @param textSpannable spannable string for whole text
     * @param textStr       whole text
     * @param textToSpan    part of text to which spans have to be applied
     * @param onClick       code that has to be run on click
     */
    public static void makeTextSpannable(@ColorInt int colorId, @NonNull final Spannable textSpannable, @NonNull String textStr,
                                         @NonNull String textToSpan, final @NonNull Runnable onClick) {
        int start = textStr.indexOf(textToSpan);
        if (start == -1) {
            Timber.e("Cannot apply spans to given string '%s' because can't find it in whole text: '%s'", textToSpan, textStr);
            return;
        }

        int end = start + textToSpan.length();
        textSpannable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        textSpannable.setSpan(new ClickableSpan() {
//            @Override
//            public void onClick(View widget) {
//                onClick.run();
//            }
//        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textSpannable.setSpan(new ForegroundColorSpan(colorId), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
