/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServerConfigResponse {

    @SerializedName("android")
    private AndroidConfig androidConfig;

    @SerializedName("donate")
    private List<Donate> donates;

    public AndroidConfig getAndroidConfig() {
        return androidConfig;
    }

    public List<Donate> getDonates() {
        return donates;
    }

    public class AndroidConfig {
        @SerializedName("hard")
        private int hardVersion;

        @SerializedName("soft")
        private int softVersion;

        @SerializedName("servertime")
        private long serverTime;

        public int getHardVersion() {
            return hardVersion;
        }

        public int getSoftVersion() {
            return softVersion;
        }

        public long getServerTime() {
            return serverTime;
        }
    }

    public class Donate {
        @SerializedName("DonationAddress")
        private String donationAddress;

        @SerializedName("FeatureCode")
        private int featureCode;

        public String getDonationAddress() {
            return donationAddress;
        }

        public int getFeatureCode() {
            return featureCode;
        }
    }
}
