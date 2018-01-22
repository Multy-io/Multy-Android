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

    public AndroidConfig getAndroidConfig() {
        return androidConfig;
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
}
