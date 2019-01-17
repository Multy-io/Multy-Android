/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.messaging.FirebaseMessaging;

import org.greenrobot.eventbus.EventBus;

import java.lang.annotation.Native;
import java.text.DecimalFormat;
import java.util.List;
import java.security.MessageDigest;

import io.multy.Multy;
import io.multy.R;
import io.multy.model.entities.OpenDragonsEvent;
import io.multy.model.entities.UserId;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.ui.Hash2PicView;
import io.multy.ui.fragments.dialogs.DonateDialog;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.multy.util.NumberFormatter;

public class PortfoliosAdapter extends PagerAdapter {

    public static String TAG_BALANCE = "balance";

    private FragmentManager fragmentManager;
    private String[] itemsName = new String[]{
            "",
            Multy.getContext().getString(R.string.currency_charts)
    };

    public PortfoliosAdapter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public int getCount() {
        return itemsName.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View layout;
        if (position == 0) {
            layout = LayoutInflater.from(container.getContext()).inflate(R.layout.item_total_balance, container, false);
            layout.setTag(TAG_BALANCE);
        } else {
            layout = LayoutInflater.from(container.getContext()).inflate(R.layout.item_portfolio, container, false);
        }
        container.addView(layout);
        ImageView imageBackground = layout.findViewById(R.id.image_background);
        TextView textDonate = layout.findViewById(R.id.text_donate);

        int donationCode = 0;
        switch (position) {
            case 0:
                updateBalanceView(layout);
                break;
            case 1:
                imageBackground.setImageResource(R.drawable.charts_donation_image);
                donationCode = Constants.DONATE_ADDING_CHARTS;
                textDonate.setText(itemsName[position]);
                break;
        }
        layout.setTag(donationCode);
        layout.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 500);
            if (position == 0) {
//                EventBus.getDefault().post(new OpenDragonsEvent());
            } else {
                DonateDialog.getInstance((Integer) v.getTag()).show(fragmentManager, DonateDialog.TAG);
            }

        });
        return layout;
    }

    public void updateBalanceView(View v) {
        TextView textBalance = v.findViewById(R.id.text_balance);
        TextView textDecimals = v.findViewById(R.id.text_decimals);
        final String balance = getTotalFiatBalance();
        final String decimals = balance.contains(".") ? balance.substring(balance.indexOf(".")) : ".00";
        final String fiat = balance.contains(".") ? balance.substring(0, balance.indexOf(".")) : balance;
        textBalance.setText(fiat);
        textDecimals.setText(decimals);
    }

    private String getTotalFiatBalance() {
        List<Wallet> wallets = RealmManager.get().copyFromRealm(RealmManager.getAssetsDao().getWallets());
        double sum = 0;

        for (Wallet wallet : wallets) {
            if (wallet.getCurrencyId() == NativeDataHelper.Blockchain.ETH.getValue() && wallet.getNetworkId() == NativeDataHelper.NetworkId.ETH_MAIN_NET.getValue()
                    || wallet.getCurrencyId() != NativeDataHelper.Blockchain.ETH.getValue() && wallet.getNetworkId() == NativeDataHelper.NetworkId.MAIN_NET.getValue()) {
                sum += Double.valueOf(wallet.getFiatBalanceLabelTrimmed().replace(wallet.getFiatString(), ""));
            }
        }

        return NumberFormatter.getFiatInstance().format(sum);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return itemsName[position];
    }
}
