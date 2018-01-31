/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

public class ServerConfigResponse {

    @SerializedName("android")
    private AndroidConfig androidConfig;

    @SerializedName("donate")
    private Donate donateInfo;

    public AndroidConfig getAndroidConfig() {
        return androidConfig;
    }

    public Donate getDonateInfo() {
        return donateInfo;
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
        @SerializedName("BTC")
        private String btcDonateAddress;

        @SerializedName("ETH")
        private String ethDonateAddress;

        public String getBtcDonateAddress() {
            return btcDonateAddress;
        }

        public String getEthDonateAddress() {
            return ethDonateAddress;
        }
    }
}
