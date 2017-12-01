/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model;

import android.content.Context;

import java.util.List;

import io.multy.model.entities.Wallet;
import io.multy.storage.DatabaseHelper;

/**
 * Created by Ihar Paliashchuk on 10.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class DataManager {

    private DatabaseHelper database;

    public DataManager(Context context) {
        this.database = new DatabaseHelper(context);
    }

    public List<Wallet> getWallets(){
       return database.getWallets();
    }

    public void saveRequestWallet(Wallet wallet){
//        database.saveWallets();
    }
}
