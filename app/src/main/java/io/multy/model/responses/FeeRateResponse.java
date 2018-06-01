/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import io.multy.Multy;
import io.multy.R;
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
            result.add(new Fee(Multy.getContext().getString(R.string.fee_very_fast), veryFast, 6, Multy.getContext().getString(R.string.ten_minutes)));
            result.add(new Fee(Multy.getContext().getString(R.string.fee_fast), fast, 10, Multy.getContext().getString(R.string.six_hours)));
            result.add(new Fee(Multy.getContext().getString(R.string.fee_medium), medium, 20, Multy.getContext().getString(R.string.five_days)));
            result.add(new Fee(Multy.getContext().getString(R.string.fee_slow), slow, 50, Multy.getContext().getString(R.string.one_week)));
            result.add(new Fee(Multy.getContext().getString(R.string.fee_very_slow), verySlow, 70, Multy.getContext().getString(R.string.two_weeks)));
            result.add(new Fee(Multy.getContext().getString(R.string.fee_custom), -1, -1, ""));
            return result;
        }

        public ArrayList<Fee> asListDonation() {
            ArrayList<Fee> result = new ArrayList<>();
            final Fee selectedFee = new Fee(Multy.getContext().getString(R.string.fee_medium), medium, 20, "5 days");
            selectedFee.setSelected(true);
            result.add(selectedFee);
            result.add(new Fee(Multy.getContext().getString(R.string.fee_very_slow), verySlow, 70, "2 weeks"));
            result.add(new Fee(Multy.getContext().getString(R.string.fee_custom), -1, -1, ""));
            return result;
        }
    }
}
