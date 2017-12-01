/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import java.util.List;

import io.multy.model.DataManager;
import io.multy.model.entities.Wallet;

/**
 * Created by Ihar Paliashchuk on 14.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class AssetSendViewModel extends BaseViewModel {

    private DataManager dataManager;
    private Wallet wallet;
    private Wallet fee;
    private double amount;
    private MutableLiveData<String> receiverAddress = new MutableLiveData<>();

    public AssetSendViewModel() {
    }

    public void setContext(Context context){
        dataManager = new DataManager(context);
    }

    public List<Wallet> getWallets(){
        return dataManager.getWallets();
    }

    public void saveWallet(Wallet wallet){
        this.wallet = wallet;
//        dataManager.saveRequestWallet(wallet);
    }

    public Wallet getWallet(){
        return wallet;
    }

    public void saveFee(Wallet fee){
        this.fee = fee;
    }

    public Wallet getFee(){
        return fee;
    }

    public void setAmount(double amount){
        this.amount = amount;
    }

    public double getAmount(){
        return amount;
    }

    public MutableLiveData<String> getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress.setValue(receiverAddress);
    }
}
