/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import io.multy.R;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.DonationFragment;
import io.multy.util.Constants;
import io.realm.RealmResults;

public class DonationActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);
        setTitle(R.string.wallet);
        getSupportFragmentManager().beginTransaction().add(R.id.container,
                DonationFragment.newInstance(getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, 0),
                        getIntent().getIntExtra(Constants.EXTRA_DONATION_CODE, 0))).commit();
    }

    public static void showDonation(Context context, int donationCode) {
        RealmResults<WalletRealmObject> wallets = RealmManager.getAssetsDao().getWallets();

        for (WalletRealmObject wallet : wallets) {
            if (wallet.getAvailableBalance() > 150) {
                context.startActivity(new Intent(context, DonationActivity.class)
                        .putExtra(Constants.EXTRA_WALLET_ID, wallet.getWalletIndex())
                        .putExtra(Constants.EXTRA_DONATION_CODE, donationCode));
                return;
            }
        }

        Toast.makeText(context, "There are no available funds for donation.", Toast.LENGTH_LONG).show();
    }
}
