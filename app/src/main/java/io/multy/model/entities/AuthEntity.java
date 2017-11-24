package io.multy.model.entities;

import com.google.gson.annotations.SerializedName;

public class AuthEntity {

    @SerializedName("username")
    private String userName;
    @SerializedName("deviceid")
    private String deviceId;
    @SerializedName("password")
    private String password;

    public AuthEntity(String userName, String deviceId, String password) {
        this.userName = userName;
        this.deviceId = deviceId;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
