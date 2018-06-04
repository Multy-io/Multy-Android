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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.Multy;
import io.multy.R;
import io.multy.ui.fragments.dialogs.DonateDialog;
import io.multy.util.Constants;

/**
 * Created by appscrunch on 16.11.17.
 */

public class PortfoliosAdapter extends PagerAdapter {

    @BindView(R.id.image_background)
    ImageView imageBackground;
    @BindView(R.id.text_donate)
    TextView textDonate;

    private FragmentManager fragmentManager;
    private String[] itemsName = new String[] {
            Multy.getContext().getString(R.string.crypto_portfolio),
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

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View layout = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_portfolio, container, false);
        container.addView(layout);
        ButterKnife.bind(this, layout);
        textDonate.setText(itemsName[position]);
        int donationCode;
        if (position == 0) {
            imageBackground.setImageResource(R.drawable.portfolio_donation_image);
            donationCode = Constants.DONATE_ADDING_PORTFOLIO;
        } else {
            imageBackground.setImageResource(R.drawable.charts_donation_image);
            donationCode = Constants.DONATE_ADDING_CHARTS;
        }
        layout.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 500);
            DonateDialog.getInstance(donationCode).show(fragmentManager, DonateDialog.TAG);
        });
        return layout;
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
