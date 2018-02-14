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

import io.multy.storage.RealmManager;
import io.multy.storage.SettingsDao;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;

public class SeedViewModel extends BaseViewModel {

    public MutableLiveData<ArrayList<String>> phrase = new MutableLiveData<>();
    public MutableLiveData<Integer> position = new MutableLiveData<>();
    public MutableLiveData<Boolean> failed = new MutableLiveData<>();
    public MutableLiveData<byte[]> binarySeed = new MutableLiveData<>();

    public void initData() {
        try {
            final int wordsPerOnce = 3;
            SettingsDao settingsDao = RealmManager.getSettingsDao();
            final String mnemonic = settingsDao.getMnemonic() == null ? NativeDataHelper.makeMnemonic() : settingsDao.getMnemonic().getMnemonic();
            final ArrayList<String> phrase = new ArrayList<>(Arrays.asList(mnemonic.split(" ")));
            final ArrayList<String> phraseToShow = new ArrayList<>(phrase.size() / wordsPerOnce);
            final byte[] seed = settingsDao.getSeed().getSeed();

            for (int i = 0; i < phrase.size(); i += wordsPerOnce) {
//                Timber.i("words %s", TextUtils.join("\n", phrase.subList(i, i + 3)));
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
