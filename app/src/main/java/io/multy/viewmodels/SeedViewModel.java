/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import java.util.ArrayList;
import java.util.Arrays;

import io.multy.Multy;
import io.multy.api.MultyApi;
import io.multy.model.DataManager;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.responses.WalletsResponse;
import io.multy.util.Constants;
import io.multy.util.FirstLaunchHelper;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SeedViewModel extends BaseViewModel {

    public MutableLiveData<ArrayList<String>> phrase = new MutableLiveData<>();
    public MutableLiveData<Integer> position = new MutableLiveData<>();
    public MutableLiveData<Boolean> failed = new MutableLiveData<>();
    public MutableLiveData<byte[]> binarySeed = new MutableLiveData<>();

    public void initData() {
        try {
            final int wordsPerOnce = 3;
            DataManager dataManager = DataManager.getInstance();
            final String mnemonic = dataManager.getMnemonic() == null ? NativeDataHelper.makeMnemonic() : dataManager.getMnemonic().getMnemonic();
            final ArrayList<String> phrase = new ArrayList<>(Arrays.asList(mnemonic.split(" ")));
            final ArrayList<String> phraseToShow = new ArrayList<>(phrase.size() / wordsPerOnce);
            final byte[] seed = dataManager.getSeed().getSeed();

            for (int i = 0; i < phrase.size(); i += wordsPerOnce) {
                Timber.i("words %s", TextUtils.join("\n", phrase.subList(i, i + 3)));
                phraseToShow.add(TextUtils.join("\n", phrase.subList(i, i + 3)));
            }

            position.setValue(0);
            binarySeed.setValue(seed);
            failed.setValue(false);
            this.phrase.setValue(phraseToShow);
        } catch (JniException exception) {
            exception.printStackTrace();
            errorMessage.setValue(exception.getMessage());
        }
    }

    public void restore(String phrase, Context context, Runnable callback) {
        try {
            isLoading.setValue(true);
            Multy.makeInitialized();
            FirstLaunchHelper.setCredentials(phrase);
            MultyApi.INSTANCE.restore().enqueue(new Callback<WalletsResponse>() {
                @Override
                public void onResponse(Call<WalletsResponse> call, Response<WalletsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getWallets() != null && response.body().getWallets().size() > 0) {
                            DataManager dataManager = DataManager.getInstance();
                            for (WalletRealmObject walletRealmObject : response.body().getWallets()) {
                                dataManager.saveWallet(walletRealmObject);
                            }
                        }
                    }
                    DataManager.getInstance().setMnemonic(new Mnemonic(phrase));
                    Prefs.putBoolean(Constants.PREF_BACKUP_SEED, true);
                    isLoading.setValue(false);
                    failed.setValue(false);
                    callback.run();
                }

                @Override
                public void onFailure(Call<WalletsResponse> call, Throwable t) {
                    isLoading.setValue(false);
                    failed.setValue(true);
                    t.printStackTrace();
                    callback.run();
                }
            });
        } catch (JniException e) {
            isLoading.setValue(false);
            failed.setValue(true);
            callback.run();
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
