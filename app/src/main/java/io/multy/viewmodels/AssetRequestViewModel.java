/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.List;

import io.multy.model.DataManager;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.requests.AddWalletAddressRequest;
import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;
import timber.log.Timber;

/**
 * Created by Ihar Paliashchuk on 14.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class AssetRequestViewModel extends BaseViewModel {

    private DataManager dataManager;
    private WalletRealmObject wallet;
    private double amount;
//    private MutableLiveData<List<WalletRealmObject>> wallets = new MutableLiveData<>();
    private MutableLiveData<Double> exchangePrice = new MutableLiveData<>();
    private MutableLiveData<String> address = new MutableLiveData<>();

    public AssetRequestViewModel() {
    }

    public void setContext(Context context){
        dataManager = new DataManager(context);
    }

    public Double getExchangePrice(){
        dataManager.getExchangePrice(CurrencyCode.BTC.name(), CurrencyCode.USD.name())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {
                    exchangePrice.setValue(response.getUSD());
                }, throwable -> {
                    errorMessage.setValue(Constants.ERROR_LOAD_EXCHANGE_PRICE);
                    errorMessage.call();
                    throwable.printStackTrace();
                });

        if (dataManager.getExchangePriceDB() != null) {
            exchangePrice.setValue(dataManager.getExchangePriceDB());
            return dataManager.getExchangePriceDB();
        } else {
            exchangePrice.setValue(16000.0);
            return 16000.0;
        }
    }

    public List<WalletRealmObject> getWallets(){
        return dataManager.getWallets();
    }

    public MutableLiveData<Double> getExchangePriceLive() {
        return exchangePrice;
    }

    public void setWallet(WalletRealmObject wallet){
        this.wallet = wallet;
    }

    public WalletRealmObject getWallet(){
        return wallet;
    }

    public void setAmount(double amount){
        this.amount = amount;
    }

    public double getAmount(){
        return amount;
    }

//    public void saveExchangePrice(){
//        dataManager.saveExchangePrice(15432.0);
//    }

    public Bitmap generateQR(Context context) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    getStringQr(),
                    BarcodeFormat.QR_CODE,
                    200, 200, null
            );
        } catch (IllegalArgumentException Illegalargumentexception) {
            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ?
                        context.getResources().getColor(android.R.color.black):
                        context.getResources().getColor(android.R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888);

        bitmap.setPixels(pixels, 0, bitMatrixWidth, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    public List<WalletRealmObject> getWalletsDB(){
        return dataManager.getWallets();
    }

    public MutableLiveData<String> getAddress() {
        return address;
    }

    public void addAddress(){
        try {
            for (WalletAddress address : wallet.getAddresses()) { // to view wallet addresses before adding new address
                Timber.i("before address %s", address);
            }

            isLoading.setValue(true);
            isLoading.call();

            final int index = wallet.getAddresses().size();
            final int currency = NativeDataHelper.Currency.BTC.getValue();
            final byte[] seed = dataManager.getSeed().getSeed();
            String creationAddress = NativeDataHelper.makeAccountAddress(seed, index, currency);

            // TODO add loading dialog
            dataManager.addWalletAddress(new AddWalletAddressRequest(wallet.getWalletIndex(), creationAddress, index))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(response -> {
                        dataManager.saveAddress(wallet, new WalletAddress(index, creationAddress));
                        address.setValue(creationAddress);

                        for (WalletAddress address : wallet.getAddresses()) { // to view wallet addresses after adding new address
                            Timber.i("after address %s", address);
                        }
                        isLoading.setValue(false);
                        isLoading.call();
                    }, throwable -> {
                        isLoading.setValue(false);
                        isLoading.call();
                        errorMessage.setValue("An error occurred while adding new address");
                        throwable.printStackTrace();
                        // TODO show error dialog
                    });

        } catch (JniException e) {
            e.printStackTrace();
        }
    }

    public String getStringQr(){
//        java.lang.IllegalAccessError: Method 'java.lang.String io.multy.viewmodels.AssetRequestViewModel.getWalletAddress()' is inaccessible to class 'io.multy.viewmodels.AssetRequestViewModel$override' (declaration of 'io.multy.viewmodels.AssetRequestViewModel$override' appears in /data/data/io.multy/files/instant-run/dex-temp/reload0x0000.dex)
        return "bitcoin:" + getWalletAddress()  + (amount == 0 ? "" : "?amount=" + amount);
    }

    public String getWalletAddress(){
        return wallet.getAddresses().isEmpty()
                ? wallet.getCreationAddress()
                : wallet.getAddresses().get(wallet.getAddresses().size() - 1).getAddress();
    }
}
