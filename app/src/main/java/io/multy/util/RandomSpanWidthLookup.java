/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.support.v7.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class RandomSpanWidthLookup extends GridLayoutManager.SpanSizeLookup {

    private static final int RANDOM_CELLS = 7;

    private int spanSize;
    private ArrayList<Integer> spans;

    public RandomSpanWidthLookup(int spanSize) {
        this.spanSize = spanSize;
        initSpans();
    }

    private void initSpans() {
        final int spanPerItem = spanSize / 8;
        int[] topSpans = new int[8];
        int[] bottomSpans = new int[8];
        spans = new ArrayList<>(topSpans.length + bottomSpans.length - 1);

        Arrays.fill(topSpans, spanPerItem);
        Arrays.fill(bottomSpans, spanPerItem);
        topSpans = randomizeArray(topSpans);
        bottomSpans = randomizeArray(bottomSpans);

        for (int value : topSpans) {
            spans.add(value);
        }

        spans.addAll(proceedBottomSpans(bottomSpans));
    }

    /**
     * transforms given array into list
     * also performs combining last elements into 1
     * and reversing whole list
     *
     * @param array
     * @return
     */
    private ArrayList<Integer> proceedBottomSpans(int[] array) {
        ArrayList<Integer> result = new ArrayList<>(array.length - 1);
        for (int i = 0; i < array.length - 1; i++) {
            if (i == array.length - 2) {
                //need to combine last 2 elements as we need only 15 for all
                spans.add(array[i] + array[array.length - 1]);
            } else {
                spans.add(array[i]);
            }
        }

        Collections.reverse(result);
        return result;
    }

    private int[] randomizeArray(int[] array) {
        Random random = new Random();
        int randomInt;

        for (int i = 0; i < array.length - 1; i++) {
            randomInt = random.nextInt(RANDOM_CELLS);
            array[i] = array[i] - randomInt;
            array[i + 1] = array[i + 1] + randomInt;
        }
        return array;
    }

    private int usedSpanCount() {
        int result = 0;
        for (Integer span : spans) {
            result += span;
        }

        return result;
    }

    @Override
    public int getSpanSize(int position) {
        return spans.get(position);
    }
}
