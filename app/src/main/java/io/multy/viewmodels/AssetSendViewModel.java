/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;

import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.Fee;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.responses.FeeRateResponse;
import io.multy.storage.RealmManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ihar Paliashchuk on 14.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class AssetSendViewModel extends BaseViewModel {

    public MutableLiveData<WalletRealmObject> wallet = new MutableLiveData<>();
    public MutableLiveData<FeeRateResponse.Speeds> speeds = new MutableLiveData<>();
    private Fee fee;
    private double amount;
    private boolean isPayForCommission;
    private MutableLiveData<String> receiverAddress = new MutableLiveData<>();
    public MutableLiveData<String> thoseAddress = new MutableLiveData<>();
    private String donationAmount;
    private boolean isAmountScanned = false;
    private CurrenciesRate currenciesRate;

    public AssetSendViewModel() {
        currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
    }

    public CurrenciesRate getCurrenciesRate() {
        return currenciesRate;
    }

    public void setWallet(WalletRealmObject wallet) {
        this.wallet.setValue(wallet);
    }

    public WalletRealmObject getWallet() {
        return this.wallet.getValue();
    }

    public void saveFee(Fee fee) {
        this.fee = fee;
    }

    public Fee getFee() {
        return fee;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public MutableLiveData<String> getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress.setValue(receiverAddress);
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

    public boolean isAmountScanned() {
        return isAmountScanned;
    }

    public void setAmountScanned(boolean amountScanned) {
        isAmountScanned = amountScanned;
    }

    public void requestFeeRates(int currencyId) {
        isLoading.setValue(true);
        MultyApi.INSTANCE.getFeeRates(currencyId).enqueue(new Callback<FeeRateResponse>() {
            @Override
            public void onResponse(Call<FeeRateResponse> call, Response<FeeRateResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    speeds.postValue(response.body().getSpeeds());
                } else {
                    errorMessage.postValue(Multy.getContext().getString(R.string.error_loading_rates));
                }
            }

            @Override
            public void onFailure(Call<FeeRateResponse> call, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue(t.getMessage());
                t.printStackTrace();
            }
        });
    }
}
