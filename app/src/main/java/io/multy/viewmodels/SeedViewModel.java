/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

import io.multy.model.DataManager;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
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
}
