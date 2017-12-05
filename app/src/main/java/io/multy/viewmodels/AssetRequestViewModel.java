/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.List;

import io.multy.model.DataManager;
import io.multy.model.entities.wallet.Wallet;

/**
 * Created by Ihar Paliashchuk on 14.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class AssetRequestViewModel extends BaseViewModel {

    private DataManager dataManager;
    private Wallet wallet;
    private double amount;

    public AssetRequestViewModel() {
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

    public void setAmount(double amount){
        this.amount = amount;
    }

    public double getAmount(){
        return amount;
    }

    public Bitmap generateQR(Context context) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
//                    wallet.getCreationAddress(),
                    "bitcoin:" + "1GLY7sDe7a6xsewDdUNA6F8CEoAxQsHV37"  + (amount == 0 ? "" : "?amount=" + amount),
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

    public String getQr(){
        return "bitcoin:" + "1GLY7sDe7a6xsewDdUNA6F8CEoAxQsHV37"  + (amount == 0 ? "" : "?amount=" + amount);
    }

}
