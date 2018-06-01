/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.realm.RealmResults;

public class WalletsPagerAdapter extends PagerAdapter {

    public interface OnInteractionsListener {
        void onWalletSelect();

        void onFling();

        void onCancel();
    }

    @BindView(R.id.text_balance_original)
    TextView textBalance;
    @BindView(R.id.text_balance_currency)
    TextView textBalanceFiat;
    @BindView(R.id.text_name)
    TextView walletName;
    @BindView(R.id.image_logo)
    ImageView imageLogo;

    private RealmResults<Wallet> wallets;
    private OnInteractionsListener listener;

    public WalletsPagerAdapter(OnInteractionsListener listener) {
        this.listener = listener;
        this.wallets = RealmManager.getAssetsDao().getAvailableWallets();
    }

    @Override
    public int getCount() {
        return wallets.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View layout = LayoutInflater.from(container.getContext()).inflate(R.layout.item_pager_wallet, container, false);
        container.addView(layout);
        ButterKnife.bind(this, layout);

        final Wallet wallet = wallets.get(position);
        walletName.setText(wallet.getWalletName());
        textBalance.setText(wallet.getBalanceLabel());
        textBalanceFiat.setText(wallet.getFiatBalanceLabel());
        imageLogo.setImageResource(wallet.getIconResourceId());

        layout.setOnClickListener(v -> {
            listener.onWalletSelect();
        });
        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public Wallet getWalletByPosition(int position) {
        return wallets.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public Wallet getItem(int position) {
        return wallets.get(position);
    }
}
