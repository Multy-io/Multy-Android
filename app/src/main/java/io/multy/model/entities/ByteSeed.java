/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ByteSeed extends RealmObject {

    @PrimaryKey
    private int id = 0;

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
}
