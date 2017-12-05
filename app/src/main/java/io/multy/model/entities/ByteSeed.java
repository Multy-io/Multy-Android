/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;


import java.util.Arrays;

import io.realm.RealmObject;

public class ByteSeed extends RealmObject {

    private byte[] seed;

    public ByteSeed() {
    }

    public ByteSeed(byte[] seed) {
        this.seed = seed;
    }

    public byte[] getSeed() {
        return seed;
    }

    public void setSeed(byte[] seed) {
        this.seed = seed;
    }

    @Override
    public String toString() {
        return "ByteSeed{" +
                "seed=" + Arrays.toString(seed) +
                '}';
    }
}
