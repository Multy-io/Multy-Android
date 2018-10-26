/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.multy.model.entities.MultisigFactory;

public class ServerConfigResponse {

    @SerializedName("android")
    private AndroidConfig androidConfig;

    @SerializedName("browserdefault")
    private BrowserDefault brouserDefault;

    @SerializedName("donate")
    private List<Donate> donates;

    @SerializedName("multisigfactory")
    private MultisigFactory multisigFactory;

    public MultisigFactory getMultisigFactory() {
        return multisigFactory;
    }

    public AndroidConfig getAndroidConfig() {
        return androidConfig;
    }

    public List<Donate> getDonates() {
        return donates;
    }

    public BrowserDefault getBrouserDefault() {
        return brouserDefault;
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

    public class BrowserDefault {
        @SerializedName("url")
        private String url;
        @SerializedName("currencyid")
        private int currencyId = -1;
        @SerializedName("networkid")
        private int networkId = -1;

        public String getUrl() {
            return url;
        }

        public int getCurrencyId() {
            return currencyId;
        }

        public int getNetworkId() {
            return networkId;
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
