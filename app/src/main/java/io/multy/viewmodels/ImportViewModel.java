/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.MultisigWallet;
import io.multy.model.entities.wallet.Owner;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletPrivateKey;
import io.multy.model.requests.WalletRequest;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.AssetsDao;
import io.multy.storage.RealmManager;
import io.multy.util.Constants;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;
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
        isLoading.setValue(false);A
        Crashlytics.logException(throwable);
        errorMessage.setValue(Multy.getContext().getString(R.string.something_went_wrong));
    }

    private void getVerbose(Action onComplete) {
        wallets.setValue(null);
        Disposable disposable = Flowable.create((FlowableOnSubscribe<List<Wallet>>) e -> {
            WalletsResponse body = MultyApi.INSTANCE.getWalletsVerbose().execute().body();
            if (body != null) {
                e.onNext(checkWalletVisibility(body.getWallets()));
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.wallets::setValue, this::onError, onComplete);
        addDisposable(disposable);
    }

    private List<Wallet> checkWalletVisibility(List<Wallet> wallets) {
        Realm realm = Realm.getInstance(Multy.getRealmConfiguration());
        AssetsDao dao = new AssetsDao(realm);
        for (Wallet wallet : wallets) {
            if (wallet.getIndex() < 0 && dao.getPrivateKey(wallet.getActiveAddress().getAddress(),
                    wallet.getCurrencyId(), wallet.getNetworkId()) == null) {
                wallet.setVisible(false);
            }
        }
        realm.close();
        wallets = checkMultisigVisibility(wallets);
        return wallets;
    }

    private List<Wallet> checkMultisigVisibility(List<Wallet> wallets) {
        for (Wallet wallet : wallets) {
            if (wallet.isMultisig()) {
                String selfAddress = getSelfMultisigAddress(wallet.getMultisigWallet());
                if (!isWalletVisible(wallets, selfAddress)) {
                    wallet.setVisible(false);
                }
            }
        }
        return wallets;
    }

    private boolean isWalletVisible(List<Wallet> wallets, String selfAddress) {
        for (Wallet wallet : wallets) {
            if (wallet.getActiveAddress().getAddress().equals(selfAddress)) {
                return wallet.isVisible();
            }
        }
        return false;
    }

    private String getSelfMultisigAddress(MultisigWallet multisigWallet) {
        for (Owner owner : multisigWallet.getOwners()) {
            if (!TextUtils.isEmpty(owner.getUserId())) {
                return owner.getAddress();
            }
        }
        return null;
    }

    private boolean isAddressExist(String address, int currencyId, int networkId, boolean onlyVisible) {
        if (wallets.getValue() == null) {
            return false;
        }
        for (Wallet wallet : wallets.getValue()) {
            if (wallet.getCurrencyId() == currencyId && wallet.getNetworkId() == networkId) {
                for (WalletAddress walletAddress : wallet.getAddresses()) {
                    if (walletAddress.getAddress().equals(address) && (!onlyVisible || wallet.isVisible())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isPrivateKeyExist(String privateKey, String walletAddress, int currencyId, int networkId) {
        WalletPrivateKey keyObject = RealmManager.getAssetsDao().getPrivateKey(walletAddress, currencyId, networkId);
        return keyObject != null && keyObject.getPrivateKey().equals(privateKey);
    }

    private void savePrivateKey(String address, String privateKey, int currencyId, int networkId) {
        RealmManager.getAssetsDao().savePrivateKey(address, privateKey, currencyId, networkId);
    }

    public void importEthWallet(String privateKey, int currencyId, int networkId, String address,
                                boolean isMultisig, Consumer<Boolean> onNext) {
        isLoading.setValue(true);
        getVerbose(() -> {
            if (isAddressExist(address, currencyId, networkId, false)) {
                isLoading.setValue(false);
                if (isPrivateKeyExist(privateKey, address, currencyId, networkId) && !isMultisig) {
                    errorMessage.setValue(Multy.getContext().getString(R.string.private_key_exist));
                } else {
                    savePrivateKey(address, privateKey, currencyId, networkId);
                    onNext.accept(true);
                }
            } else {
                Disposable disposable = Flowable.create((FlowableOnSubscribe<Boolean>) e -> {
                    WalletRequest request = WalletRequest.getBulilder()
                            .setCurrencyId(currencyId)
                            .setNetworkId(networkId)
                            .setAddress(address)
                            .setWalletName(Constants.DEFAULT_IMPORT_WALLET_NAME)
                            .setImported(true)
                            .build();
                    Response<ResponseBody> response = MultyApi.INSTANCE.importWallet(request).execute();
                    isLoading.postValue(false);
                    e.onNext(response.isSuccessful());
                }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(isSuccess -> {
                            if (isSuccess) {
                                savePrivateKey(address, privateKey, currencyId, networkId);
                            }
                            onNext.accept(isSuccess);
                        }, this::onError);
                addDisposable(disposable);
            }
        });
    }

    public void importEthMultisigWallet(int currencyId, int networkId, String linkedAddress,
                                        String multisigAddress, Consumer<Boolean> onNext) throws Exception {
        isLoading.setValue(true);
        if (isAddressExist(multisigAddress, currencyId, networkId, false)) {
            if (isAddressExist(multisigAddress, currencyId, networkId, true)) {
                errorMessage.setValue(Multy.getContext().getString(R.string.multisig_imported));
            } else {
                Wallet wallet = getWalletWithAddress(multisigAddress);
                if (wallet == null || !wallet.isMultisig()) {
                    onNext.accept(false);
                } else if (!isOwnersContainsAddress(wallet.getMultisigWallet().getOwners(), linkedAddress)) {
                    errorMessage.setValue(Multy.getContext().getString(R.string.invalid_key_for_multisig));
                } else {
                    onNext.accept(true);
                }
            }
            isLoading.setValue(false);
        } else {
            Disposable disposable = Flowable.create((FlowableOnSubscribe<Boolean>) e -> {
                WalletRequest request = WalletRequest.getBulilder()
                        .setCurrencyId(currencyId)
                        .setNetworkId(networkId)
                        .setAddress(linkedAddress)
                        .setWalletName(Constants.DEFAULT_IMPORT_MULTISIG_NAME)
                        .setImported(true)
                        .setMultisig(WalletRequest.Multisig.getBuilder()
                                .setMultisigAddress(multisigAddress)
                                .setIsMultisig(true)
                                .setIsImported(true)
                                .build())
                        .build();
                Response<ResponseBody> response = MultyApi.INSTANCE.importMultisigWallet(request).execute();
                isLoading.postValue(false);
                e.onNext(response.isSuccessful());
            }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onNext, this::onError);
            addDisposable(disposable);
        }
    }

    private Wallet getWalletWithAddress(String address) {
        if (this.wallets.getValue() != null) {
            for (Wallet wallet : wallets.getValue()) {
                if (wallet.getActiveAddress().getAddress().equals(address)) {
                    return wallet;
                }
            }
        }
        return null;
    }

    private boolean isOwnersContainsAddress(RealmList<Owner> owners, String address) {
        for (Owner owner : owners) {
            if (owner.getAddress().equals(address)) {
                return true;
            }
        }
        return false;
    }
}
