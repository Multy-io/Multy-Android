/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by anschutz1927@gmail.com on 05.09.18.
 */
public class MultisigInfo {

    @SerializedName("confirmed")
    private boolean confirmed;
    @SerializedName("contract")
    private String contract;
    @SerializedName("input")
    private String input;
    @SerializedName("invocationstatus")
    private boolean invocationStatus;
    @SerializedName("methodinvoked")
    private String methodInvoked;
    @SerializedName("return")
    private String return_;
    @SerializedName("requestid")
    private String requestId;

    @SerializedName("owners")
    private ArrayList<TransactionOwner> owners;

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getContract() {
        return contract;
    }

    public String getInput() {
        return input;
    }

    public boolean isInvocationStatus() {
        return invocationStatus;
    }

    public String getMethodInvoked() {
        return methodInvoked;
    }

    public String getReturn() {
        return return_;
    }

    public ArrayList<TransactionOwner> getOwners() {
        return owners;
    }

    public String getRequestId() {
        return requestId;
    }
}
