/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;


import io.multy.util.SingleLiveEvent;

public class ContactsViewModel extends BaseViewModel {

    private SingleLiveEvent<Boolean> notifyData = new SingleLiveEvent<>();

    public SingleLiveEvent<Boolean> getNotifyData() {
        return notifyData;
    }

    public void setNotifyData(Boolean notifyData) {
        this.notifyData.setValue(notifyData);
    }
}
