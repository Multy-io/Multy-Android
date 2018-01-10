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
import io.multy.model.entities.Fee;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.storage.RealmManager;

/**
 * Created by Ihar Paliashchuk on 14.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class AssetSendViewModel extends BaseViewModel {

    private DataManager dataManager;
    private WalletRealmObject wallet;
    private Fee fee;
    private double amount;
    private boolean isPayForCommission;
    private MutableLiveData<String> receiverAddress = new MutableLiveData<>();
    private MutableLiveData<Double> exchangePrice = new MutableLiveData<>();
    public MutableLiveData<String> thoseAddress = new MutableLiveData<>();
    private String donationAmount;
    private boolean isAmountScanned = false;

    public AssetSendViewModel() {
    }

    public void setContext(Context context){
        dataManager = DataManager.getInstance();
    }

    public void auth(){
        dataManager.auth("dsdbsn", "sfgn", "asdfah");
    }

    @Deprecated
    public void getApiExchangePrice(){
        RealmManager.getSettingsDao().getExchangePrice().getExchangePrice();
    }

    public void setWallet(WalletRealmObject wallet){
        this.wallet = wallet;
    }

    public WalletRealmObject getWallet(){
        return wallet;
    }

    public void saveFee(Fee fee){
        this.fee = fee;
    }

    public Fee getFee(){
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

    public MutableLiveData<Double> getExchangePrice() {
        return exchangePrice;
    }

    public String getDonationAmount() {
        return donationAmount;
    }

    public void setDonationAmount(String donationAmount) {
        this.donationAmount = donationAmount;
    }

    public boolean isPayForCommission() {
        return isPayForCommission;
    }

    public void setPayForCommission(boolean payForCommission) {
        isPayForCommission = payForCommission;
    }

    public List<WalletRealmObject> getWalletsDB(){
        return dataManager.getWallets();
    }

    public boolean isAmountScanned() {
        return isAmountScanned;
    }

    public void setAmountScanned(boolean amountScanned) {
        isAmountScanned = amountScanned;
    }

    public void getWalletFromDB(int index) {
        this.wallet = dataManager.getWallet(index);
    }
}
