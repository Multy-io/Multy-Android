/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import io.realm.RealmObject;

public class RootKey extends RealmObject {

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

    @Override
    public String toString() {
        return "RootKey{" +
                "key='" + key + '\'' +
                '}';
    }
}
