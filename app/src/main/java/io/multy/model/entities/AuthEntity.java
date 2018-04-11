/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

public class AuthEntity {

    @SerializedName("userID")
    private String userId;
    @SerializedName("deviceID")
    private String deviceId;
    @SerializedName("pushToken")
    private String token;
    @SerializedName("deviceType")
    private int deviceType;

    public AuthEntity(String userId, String deviceId, String token, int deviceType) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.token = token;
        this.deviceType = deviceType;
    }

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
}
