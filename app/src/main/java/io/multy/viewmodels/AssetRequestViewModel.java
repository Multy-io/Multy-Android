/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.List;

import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.requests.AddWalletAddressRequest;
import io.multy.storage.RealmManager;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetRequestViewModel extends BaseViewModel {

    private Wallet wallet;
    private double amount;
    private MutableLiveData<String> address = new MutableLiveData<>();
    private MutableLiveData<Wallet> walletLive = new MutableLiveData<>();

    public AssetRequestViewModel() {
    }

    public Double getExchangePrice() {
        return RealmManager.getSettingsDao().getCurrenciesRateById(wallet.getCurrencyId());
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public Wallet getWallet(long id) {
        this.wallet = RealmManager.getAssetsDao().getWalletById(id);
        walletLive.setValue(wallet);
        return wallet;
    }

    public MutableLiveData<Wallet> getWalletLive() {
        return walletLive;
    }

    public void generateQr(String strQr, int colorDark, int colorLight,
                           Consumer<Bitmap> consumerNext, Consumer<Throwable> consumerError) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Bitmap>) e -> {
            try {
                Bitmap bitmap = generateQr(strQr, colorDark, colorLight);
                e.onNext(bitmap);
            } catch (Throwable throwable) {
                e.onError(throwable);
            }
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumerNext, consumerError);
        addDisposable(disposable);
    }

    private Bitmap generateQr(String strQr, int colorDark, int colorLight) throws Throwable {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(strQr, BarcodeFormat.QR_CODE, 200, 200, null);
        final int bitMatrixWidth = bitMatrix.getWidth();
        final int bitMatrixHeight = bitMatrix.getHeight();
        final int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];
        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? colorDark : colorLight;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, bitMatrixWidth, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    public List<Wallet> getWalletsDB() {
        return RealmManager.getAssetsDao().getWallets();
    }

    public MutableLiveData<String> getAddress() {
        return address;
    }

    public void getBtcAddresses() {
        try {
            isLoading.setValue(true);
            isLoading.call();

            final int addressIndex = wallet.getBtcWallet().getAddresses().size();
            final byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();
            final String creationAddress = NativeDataHelper.makeAccountAddress(seed, wallet.getIndex(), addressIndex,
                    NativeDataHelper.Blockchain.BTC.getValue(), NativeDataHelper.NetworkId.TEST_NET.getValue());

            MultyApi.INSTANCE.addWalletAddress(new AddWalletAddressRequest(wallet.getIndex(), creationAddress, addressIndex, wallet.getNetworkId(), wallet.getCurrencyId())).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    RealmManager.getAssetsDao().saveBtcAddress(wallet.getId(), new WalletAddress(addressIndex, creationAddress));
                    address.setValue(creationAddress);
                    isLoading.setValue(false);
                    isLoading.call();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    isLoading.setValue(false);
                    isLoading.call();
                    errorMessage.setValue("An error occurred while adding new address");
                    t.printStackTrace();
                }
            });

        } catch (JniException e) {
            e.printStackTrace();
        }
    }

    public String getStringQr() {
        return "bitcoin:" + getWalletAddress() + (amount == 0 ? "" : "?amount=" + amount);
    }

    public String getStringAddress() {
        return "bitcoin:" + getWalletAddress();
    }

    public String getStringAmount() {
        return String.valueOf(amount);
    }

    public String getWalletAddress() {
        return wallet.getActiveAddress().getAddress();
    }

    public int getChainId() {
        return 1;
    }
}
