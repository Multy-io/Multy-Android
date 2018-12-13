/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

public class MessageResponse {

    @SerializedName("code")
    private int code;
    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    //    @SerializedName("message")
//    private ResponseMessage responseMessage;
//
//    public int getCode() {
//        return code;
//    }
//
//    public ResponseMessage getResponseMessage() {
//        return responseMessage;
//    }
//
//    public class ResponseMessage {
//        @SerializedName("message")
//        public String message;
//    }
}
