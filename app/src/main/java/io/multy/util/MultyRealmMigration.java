/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import io.multy.model.entities.wallet.RecentAddress;
import io.realm.DynamicRealm;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class MultyRealmMigration implements io.realm.RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            final RealmSchema schema = realm.getSchema();
            if (oldVersion== 1) {
                final RealmObjectSchema recentAddressSchema = schema.get(RecentAddress.class.getSimpleName());
                recentAddressSchema.addField(RecentAddress.RECENT_ADDRESS_ID, long.class);
            }
    }

    @Override
    public int hashCode() {
        return 37;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof MultyRealmMigration);
    }

}
