/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by anschutz1927@gmail.com on 07.03.18.
 */

public class DonateFeatureEntity extends RealmObject {

    public static final String FEATURE_CODE = "featureCode";
    public static final String DONATION_ADDRESS = "donationAddress";

    @PrimaryKey
    private int featureCode;
    private String donationAddress;

    public DonateFeatureEntity() {}

    public DonateFeatureEntity(int featureCode) {
        this.featureCode = featureCode;
    }

    public int getFeatureCode() {
        return featureCode;
    }

    public String getDonationAddress() {
        return donationAddress;
    }

    public void setDonationAddress(String donationAddress) {
        this.donationAddress = donationAddress;
    }
}
