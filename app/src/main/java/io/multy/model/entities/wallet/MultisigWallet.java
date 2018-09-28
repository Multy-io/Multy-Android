/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class MultisigWallet extends RealmObject {

//            "owners": [
//    {
//        "userid": "006ec989583adb56b2eb6b2a7e4b979ed622923f59582059a34f908e8f5d416609",
//            "address": "0xdca54d8a8291a033366ad922d0ecce3944b693c9",
//            "associated": true,
//            "creator": true,
//            "walletIndex": 1,
//            "addressIndex": 0
//    }
//        ],
//                "confirmations": 2,
//                "deployStatus": 1,
//                "inviteCode": "BoRBTuh6CF1jIDzGCirUJP5Rwuy8S8AZEQGBFTZAyZA=\n",
//                "ownersCount": 2

    @SerializedName("owners")
    private RealmList<Owner> owners;
    @SerializedName("confirmations")
    private int confirmations;
    @SerializedName("inviteCode")
    private String inviteCode;
    @SerializedName("ownersCount")
    private int ownersCount;
    @SerializedName("deployStatus")
    private int deployStatus;

    @SerializedName("nonce")
    private String nonce = "0";
    @SerializedName("balance")
    private String balance = "0";
    @SerializedName("pendingbalance")
    private String pendingBalance = "0";
    @SerializedName("havePaymentReqests")
    private boolean havePaymentRequests = false;

    public RealmList<Owner> getOwners() {
        return owners;
    }

    public void setOwners(RealmList<Owner> owners) {
        this.owners = owners;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public int getOwnersCount() {
        return ownersCount;
    }

    public void setOwnersCount(int ownersCount) {
        this.ownersCount = ownersCount;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getPendingBalance() {
        return pendingBalance;
    }

    public void setPendingBalance(String pendingBalance) {
        this.pendingBalance = pendingBalance;
    }

    public int getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(int deployStatus) {
        this.deployStatus = deployStatus;
    }

    public boolean isHavePaymentRequests() {
        return havePaymentRequests;
    }

    public void setHavePaymentRequests(boolean havePaymentRequests) {
        this.havePaymentRequests = havePaymentRequests;
    }

    public MultisigWallet asRealmObject(Realm realm) {
        MultisigWallet multisigWallet = realm.createObject(MultisigWallet.class);
        multisigWallet.setOwners(new RealmList<>());

        for (Owner owner : getOwners()) {
            multisigWallet.getOwners().add(realm.copyToRealm(owner));
        }

        multisigWallet.setNonce(nonce);
        multisigWallet.setPendingBalance(pendingBalance);
        multisigWallet.setBalance(balance);
        multisigWallet.setConfirmations(confirmations);
        multisigWallet.setDeployStatus(deployStatus);
        multisigWallet.setInviteCode(inviteCode);
        multisigWallet.setOwnersCount(ownersCount);
        multisigWallet.setHavePaymentRequests(havePaymentRequests);
        return multisigWallet;
    }

    public static class Status {
        //there is no documentation don't know how it works
        public static final int CREATED = 1;
        public static final int READY = 2;
        public static final int PENDING = 3;
        public static final int REJECTED = 4;
        public static final int DEPLOYED = 5;
    }
}
