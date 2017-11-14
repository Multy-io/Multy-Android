/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 * Created by andre on 08.11.2017.
 */

public class SeedViewModel extends ViewModel {

    private MutableLiveData<String[]> phrase = new MutableLiveData<>();
    private MutableLiveData<Integer> position = new MutableLiveData<>();
    private MutableLiveData<byte []> binarySeed = new MutableLiveData<>();

    public MutableLiveData<Integer> getPosition() {
        return position;
    }

    public void setPosition(MutableLiveData<Integer> position) {
        this.position = position;
    }

    public MutableLiveData<String[]> getPhrase() {
        return phrase;
    }

    public void setPhrase(MutableLiveData<String[]> phrase) {
        this.phrase = phrase;
    }

    public MutableLiveData<byte[]> getBinarySeed() {
        return binarySeed;
    }

    public void setBinarySeed(MutableLiveData<byte[]> binarySeed) {
        this.binarySeed = binarySeed;
    }
}
