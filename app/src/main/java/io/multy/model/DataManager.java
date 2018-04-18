///*
// * Copyright 2018 Idealnaya rabota LLC
// * Licensed under Multy.io license.
// * See LICENSE for details
// */
//
//package io.multy.model;
//
//import java.util.List;
//
//import io.multy.Multy;
//import io.multy.model.entities.ByteSeed;
//import io.multy.model.entities.DeviceId;
//import io.multy.model.entities.Mnemonic;
//import io.multy.model.entities.UserId;
//import io.multy.model.entities.wallet.WalletRealmObject;
//import io.multy.storage.DatabaseHelper;
//import io.realm.RealmResults;
//
///**
// * Created by Ihar Paliashchuk on 10.11.2017.
// * ihar.paliashchuk@gmail.com
// */
//
//@Deprecated
//public class DataManager {
//
//    private static DatabaseHelper databaseHelper;
//    private static DataManager dataManager;
//
//    public static DataManager getInstance() {
//        if (dataManager == null) {
//            dataManager = new DataManager();
//        }
//
//        dataManager.refreshDatabaseHelper();
//        return dataManager;
//    }
//
//    public void refreshDatabaseHelper() {
//        databaseHelper = new DatabaseHelper(Multy.getContext());
//    }
//
//    public void saveUserId(UserId userId) {
//        databaseHelper.saveUserId(userId);
//    }
//
//    public UserId getUserId() {
//        return databaseHelper.getUserId();
//    }
//
//    public void saveSeed(ByteSeed seed) {
//        databaseHelper.saveSeed(seed);
//    }
//
//    public ByteSeed getSeed() {
//        return databaseHelper.getSeed();
//    }
//
//    public void saveWallets(List<WalletRealmObject> wallet) {
//        databaseHelper.saveWallets(wallet);
//    }
//
//    public WalletRealmObject getWallet() {
//        return databaseHelper.getWallet();
//    }
//
//    public RealmResults<WalletRealmObject> getWallets() {
//        return databaseHelper.getWallets();
//    }
//
//    public WalletRealmObject getWalletById(int walletId) {
//        return databaseHelper.getWallet();
//    }
//
//    public WalletRealmObject getWallet(int id) {
//        return databaseHelper.getWalletById(id);
//    }
//
//    public void setMnemonic(Mnemonic mnemonic) {
//        databaseHelper.setMnemonic(mnemonic);
//    }
//
//    public Mnemonic getMnemonic() {
//        return databaseHelper.getMnemonic();
//    }
//
//    public void setDeviceId(DeviceId deviceId) {
//        databaseHelper.setDeviceId(deviceId);
//    }
//}
