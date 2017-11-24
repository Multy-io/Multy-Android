package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.util.Date;

import javax.annotation.Nullable;

import io.multy.util.DateHelper;

public class AuthResponse {

    @SerializedName("expire")
    private String expireDate;
    @SerializedName("token")
    private String token;

    @Nullable
    public Date getExpireDate() {
        try {
            return DateHelper.DATE_FORMAT_AUTH.parse(expireDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getToken() {
        return token;
    }
}
