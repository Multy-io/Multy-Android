/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Rates {

    @SerializedName("VerySlow")
    private int verySlow;
    @SerializedName("Slow")
    private int slow;
    @SerializedName("Medium")
    private int medium;
    @SerializedName("Fast")
    private int fast;
    @SerializedName("VeryFast")
    private int veryFast;

    public void getAsList() {
        List<Integer> result = new ArrayList<>();
        result.add(verySlow);
        result.add(slow);
        result.add(medium);
        result.add(fast);
        result.add(veryFast);
    }
}
