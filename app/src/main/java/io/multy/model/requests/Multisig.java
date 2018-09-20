/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.requests;

import com.google.gson.annotations.SerializedName;

public class Multisig {

    @SerializedName("isMultisig")
    private boolean isMultisig = true;
    @SerializedName("signaturesRequired")
    private int signatureRequired;
    @SerializedName("ownersCount")
    private int ownersCount;
    @SerializedName("inviteCode")
    private String inviteCode;
    @SerializedName("isImported")
    private boolean isImported = false;

    public Multisig(int confirmsCount, int membersCount, String inviteCode) {
        signatureRequired = confirmsCount;
        ownersCount = membersCount;
        this.inviteCode = inviteCode;
    }

    public boolean isMultisig() {
        return isMultisig;
    }

    public void setMultisig(boolean multisig) {
        isMultisig = multisig;
    }

    public boolean isImported() {
        return isImported;
    }

    public void setImported(boolean imported) {
        isImported = imported;
    }

    public int getSignatureRequired() {
        return signatureRequired;
    }

    public void setSignatureRequired(int signatureRequired) {
        this.signatureRequired = signatureRequired;
    }

    public int getOwnersCount() {
        return ownersCount;
    }

    public void setOwnersCount(int ownersCount) {
        this.ownersCount = ownersCount;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}