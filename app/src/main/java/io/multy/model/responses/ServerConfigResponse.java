/*
 * Copyright 2017 Idealnaya rabota LLC
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
    private List<Donate> donate;

    public AndroidConfig getAndroidConfig() {
        return androidConfig;
    }

    public List<Donate> getDonate() {
        return donate;
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

        @SerializedName("Feature")
        private String featureDescription;

        @SerializedName("FeatureCode")
        private int featureCode;

        @SerializedName("OS")
        private int os;

        public String getDonationAddress() {
            return donationAddress;
        }

        public String getFeatureDescription() {
            return featureDescription;
        }

        public int getFeatureCode() {
            return featureCode;
        }

        public int getOs() {
            return os;
        }
    }
}
