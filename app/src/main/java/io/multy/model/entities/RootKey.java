/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RootKey extends RealmObject {

    @PrimaryKey
    private int id = 0;
    private String key;

    public RootKey() {
    }

    public RootKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
