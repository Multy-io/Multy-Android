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
    public static String TAG_MULTIRECEIVER = "multireceiver";
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    private LottieAnimationView animationView;
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
            layout = LayoutInflater.from(container.getContext()).inflate(R.layout.item_multi_receiver, container, false);
            layout.setTag(TAG_MULTIRECEIVER);
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
//                imageBackground.setImageResource(R.drawable.charts_donation_image);
//                donationCode = Constants.DONATE_ADDING_CHARTS;
//                textDonate.setText(itemsName[position]);
                updateMultireceiverView(layout);
                break;
        }
        layout.setTag(donationCode);
        layout.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 500);
            if (position == 0) {
//                EventBus.getDefault().post(new OpenDragonsEvent());
            } else {
//                DonateDialog.getInstance((Integer) v.getTag()).show(fragmentManager, DonateDialog.TAG);
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

    public void updateMultireceiverView(View v) {
        Hash2PicView mrIdIcon = v.findViewById(R.id.mr_icon);
        mrIdIcon.setAvatar(getMultireceiverAvatar());
        LottieAnimationView wavesAnimation = v.findViewById(R.id.animation_view);
        wavesAnimation.playAnimation();
    }

    private String getMultireceiverAvatar() {
        String userId = RealmManager.getSettingsDao().getUserId().getUserId();
        String userIdHex = md5(userId);

        return stringToHex(userIdHex.getBytes());
    }

    private String stringToHex(byte[] buf) {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
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
