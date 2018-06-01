/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import java.util.ArrayList;

import io.multy.model.entities.FastReceiver;

public class FastReceiversResponse {

    private ArrayList<FastReceiver> receivers;

    public ArrayList<FastReceiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(ArrayList<FastReceiver> receivers) {
        this.receivers = receivers;
    }
}
