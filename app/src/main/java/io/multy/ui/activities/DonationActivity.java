/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.DonationFragment;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.realm.RealmResults;

public class DonationActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);
        setTitle(R.string.wallet);
        getSupportFragmentManager().beginTransaction().add(R.id.container,
                DonationFragment.newInstance(getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0),
                        getIntent().getIntExtra(Constants.EXTRA_DONATION_CODE, 0))).commit();
    }

    public static void showDonation(Context context, int donationCode) {
        if (!Prefs.getBoolean(Constants.PREF_APP_INITIALIZED, false)) {
            Toast.makeText(context, "You are have no wallets, let's make the first wallet!", Toast.LENGTH_SHORT).show();
            return;
        }

        RealmResults<Wallet> wallets = RealmManager.getAssetsDao().getWallets();

        for (Wallet wallet : wallets) {
            if (wallet.isPayable() && wallet.getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()
                    && wallet.getNetworkId() == NativeDataHelper.NetworkId.MAIN_NET.getValue()
                    && wallet.getAvailableBalanceNumeric().longValue() >= Constants.DONATION_MIN_VALUE) {
                context.startActivity(new Intent(context, DonationActivity.class).putExtra(Constants.EXTRA_WALLET_ID, wallet.getId()).putExtra(Constants.EXTRA_DONATION_CODE, donationCode));
                return;
            }
        }

        Toast.makeText(context, "There are no available funds for donation.", Toast.LENGTH_LONG).show();
    }
}
