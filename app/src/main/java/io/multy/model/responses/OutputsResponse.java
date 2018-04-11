/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import io.multy.model.entities.Output;

public class OutputsResponse {

    @SerializedName("outs")
    ArrayList<Output> outputs;

    public ArrayList<Output> getOutputs() {
        return outputs;
    }

    public void setOutputs(ArrayList<Output> outputs) {
        this.outputs = outputs;
    }
}
