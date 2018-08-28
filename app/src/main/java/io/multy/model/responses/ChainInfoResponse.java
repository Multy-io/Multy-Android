/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

public class ChainInfoResponse extends BaseResponse {

    @SerializedName("state")
    private State state;

    public State getState() {
        return state;
    }

    class State {
        @SerializedName("chainTime")
        private String chainTime;
        @SerializedName("refBlockPrefix")
        private String refBlockPrefix;
        @SerializedName("blockNumber")
        private int blockNumber;

        public String getChainTime() {
            return chainTime;
        }

        public String getRefBlockPrefix() {
            return refBlockPrefix;
        }

        public int getBlockNumber() {
            return blockNumber;
        }
    }
}
