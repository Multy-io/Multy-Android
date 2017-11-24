/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;
import android.util.Base64;

import com.samwolfand.oneprefs.Prefs;

import java.util.ArrayList;
import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by andre on 08.11.2017.
 */

public class SeedViewModel extends ViewModel {

    public MutableLiveData<ArrayList<String>> phrase = new MutableLiveData<>();
    public MutableLiveData<Integer> position = new MutableLiveData<>();
    public MutableLiveData<Boolean> failed = new MutableLiveData<>();
    private MutableLiveData<byte[]> binarySeed = new MutableLiveData<>();

    public void initData() {
        final int wordsPerOnce = 3;
        final String mnemonic = Prefs.getString("mnemonic", "");
        final ArrayList<String> phrase = new ArrayList<>(Arrays.asList(mnemonic.split(" ")));
        final ArrayList<String> phraseToShow = new ArrayList<>(phrase.size() / wordsPerOnce);
        final byte[] seed = Base64.decode(Prefs.getString("seed", ""), Base64.DEFAULT);

        for (int i = 0; i < phrase.size(); i += wordsPerOnce) {
            Timber.i("words %s", TextUtils.join("\n", phrase.subList(i, i + 3)));
            phraseToShow.add(TextUtils.join("\n", phrase.subList(i, i + 3)));
        }

        position.setValue(0);
        binarySeed.setValue(seed);
        failed.setValue(false);
        this.phrase.setValue(phraseToShow);
    }

}
