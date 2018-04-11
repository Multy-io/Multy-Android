/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import io.multy.model.entities.Fee;

public class FeeRateResponse {

    @SerializedName("speeds")
    private Speeds speeds;

    public Speeds getSpeeds() {
        return speeds;
    }

    public class Speeds {

        @SerializedName("VerySlow")
        private long verySlow;
        @SerializedName("Slow")
        private long slow;
        @SerializedName("Medium")
        private long medium;
        @SerializedName("Fast")
        private long fast;
        @SerializedName("VeryFast")
        private long veryFast;

        public long getVerySlow() {
            return verySlow;
        }

        public long getSlow() {
            return slow;
        }

        public long getMedium() {
            return medium;
        }

        public long getFast() {
            return fast;
        }

        public long getVeryFast() {
            return veryFast;
        }

        public ArrayList<Fee> asList() {
            ArrayList<Fee> result = new ArrayList<>();
            result.add(new Fee("Very Fast", veryFast, 6, "10 min"));
            result.add(new Fee("Fast", fast, 10, "6 hours"));
            result.add(new Fee("Medium", medium, 20, "5 days"));
            result.add(new Fee("Slow", slow, 50, "1 week"));
            result.add(new Fee("Very Slow", verySlow, 70, "2 weeks"));
            result.add(new Fee("Custom", -1, -1, ""));
            return result;
        }

        public ArrayList<Fee> asListDonation() {
            ArrayList<Fee> result = new ArrayList<>();
            final Fee selectedFee = new Fee("Medium", medium, 20, "5 days");
            selectedFee.setSelected(true);
            result.add(selectedFee);
            result.add(new Fee("Very Slow", verySlow, 70, "2 weeks"));
            result.add(new Fee("Custom", -1, -1, ""));
            return result;
        }
    }
}
