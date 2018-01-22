/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class MyRealmMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        Log.i("wise", "oldVersion " + oldVersion);
        if (oldVersion == 0) {
            schema.get("WalletRealmObject").addField("pendingBalance", Double.class);
            oldVersion++;
        }

        if (oldVersion == 2) {
            schema.get("WalletRealmObject").addField("pendingBalance", Double.class);
            oldVersion++;
        }
    }

    @Override
    public int hashCode() {
        return 37;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof MyRealmMigration);
    }
}