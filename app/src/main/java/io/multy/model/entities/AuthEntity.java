/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

import io.multy.BuildConfig;

public class AuthEntity {

    @SerializedName("userID")
    private String userId;
    @SerializedName("deviceID")
    private String deviceId;
    @SerializedName("pushToken")
    private String token;
    @SerializedName("deviceType")
    private int deviceType;
    @SerializedName("appVersion")
    private String appVersion = BuildConfig.VERSION_NAME;
    @SerializedName("seedPhraseType")
    private int seedPhraseType;

    public AuthEntity(String userId, String deviceId, String token, int deviceType, int seedPhraseType) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.token = token;
        this.deviceType = deviceType;
        this.seedPhraseType = seedPhraseType;
    }

//    public AuthEntity(String userId, String deviceId, String token, int deviceType) {
//        this.userId = userId;
//        this.deviceId = deviceId;
//        this.token = token;
//        this.deviceType = deviceType;
//    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userName) {
        this.userId = userName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}
