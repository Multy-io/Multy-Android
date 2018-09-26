/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.requests.ImportWalletRequest;
import io.multy.model.requests.Multisig;
import io.multy.model.responses.WalletsResponse;
import io.multy.util.Constants;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class ImportViewModel extends BaseViewModel {

    private MutableLiveData<List<Wallet>> wallets = new MutableLiveData<>();

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    private void onError(Throwable throwable) {
        throwable.printStackTrace();
        isLoading.setValue(false);
        errorMessage.setValue(throwable.getLocalizedMessage());
    }

    private void getVerbose(Action onComplete) {
        wallets.setValue(null);
        Disposable disposable = Flowable.create((FlowableOnSubscribe<List<Wallet>>) e -> {
            WalletsResponse body = MultyApi.INSTANCE.getWalletsVerbose().execute().body();
            if (body != null) {
                e.onNext(body.getWallets());
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.wallets::setValue, this::onError, onComplete);
        addDisposable(disposable);
    }

    private boolean isAddressExist(String address) {
        if (wallets.getValue() == null) {
            return false;
        }
        for (Wallet wallet : wallets.getValue()) {
            for (WalletAddress walletAddress : wallet.getAddresses()) {
                if (walletAddress.getAddress().equals(address)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void importEthWallet(int currencyId, int networkId, String address, Consumer<Boolean> onNext) {
        isLoading.setValue(true);
        getVerbose(() -> {
            if (isAddressExist(address)) {
                onNext.accept(true);
                isLoading.setValue(false);
            } else {
                Disposable disposable = Flowable.create((FlowableOnSubscribe<Boolean>) e -> {
                    ImportWalletRequest request = new ImportWalletRequest();
                    request.setCurrencyId(currencyId);
                    request.setNetworkId(networkId);
                    request.setAddress(address);
                    request.setWalletName(Constants.DEFAULT_IMPORT_WALLET_NAME);
                    request.setImported(true);
                    Response<ResponseBody> response = MultyApi.INSTANCE.importWallet(request).execute();
                    isLoading.postValue(false);
                    e.onNext(response.isSuccessful());
                }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(onNext, this::onError);
                addDisposable(disposable);
            }
        });
    }

    public void importEthMultisigWallet(int currencyId, int networkId,
                                        String linkedAddress, String multisigAddress, Consumer<Boolean> onNext) {
        isLoading.setValue(true);
        if (isAddressExist(multisigAddress)) {
            try {
                onNext.accept(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isLoading.setValue(false);
        } else {
            Disposable disposable = Flowable.create((FlowableOnSubscribe<Boolean>) e -> {
                ImportWalletRequest request = new ImportWalletRequest();
                request.setCurrencyId(currencyId);
                request.setNetworkId(networkId);
                request.setAddress(linkedAddress);
                request.setWalletName(Constants.DEFAULT_IMPORT_MULTISIG_NAME);
                request.setImported(true);
                request.setMultisig(new Multisig(multisigAddress));
                Response<ResponseBody> response = MultyApi.INSTANCE.importMultisigWallet(request).execute();
                isLoading.postValue(false);
                e.onNext(response.isSuccessful());
            }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onNext, this::onError);
            addDisposable(disposable);
        }
    }
}
