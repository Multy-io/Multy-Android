/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.storage;

import android.content.Context;

import java.util.Arrays;

import io.multy.encryption.MasterKeyGenerator;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Ihar Paliashchuk on 10.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class DatabaseHelper {

    private Realm realm;

    public DatabaseHelper(Context context) {
        realm = Realm.getInstance(getRealmConfiguration(context));
    }

    private RealmConfiguration getRealmConfiguration(Context context){
        return new RealmConfiguration.Builder()
                .encryptionKey(Arrays.copyOfRange(MasterKeyGenerator.generateKey(context).getBytes(),
                        0 , MasterKeyGenerator.generateKey(context).getBytes().length - 1))
                .build();
    }

    public void saveWallets(){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
//                realm.insertOrUpdate();
            }
        });
    }

//    public void saveTransactions();
//    public void saveRootKey();
//    public void saveTransactions();


}
