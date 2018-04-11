/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import io.multy.model.socket.Transaction;

public class TransactionViewModel extends ViewModel {

    public MutableLiveData<Transaction> transactionData = new MutableLiveData<>();

}
