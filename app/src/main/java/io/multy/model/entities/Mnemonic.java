/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Mnemonic extends RealmObject {

    @PrimaryKey
    private int id = 0;

    private String mnemonic;

    public Mnemonic() {
    }

    public Mnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }
}
