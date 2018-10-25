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

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.Multy;
import io.multy.R;
import io.multy.model.entities.OpenDragonsEvent;
import io.multy.ui.fragments.dialogs.DonateDialog;
import io.multy.util.Constants;

/**
 * Created by appscrunch on 16.11.17.
 */

public class PortfoliosAdapter extends PagerAdapter {


    private FragmentManager fragmentManager;
    private String[] itemsName = new String[]{
            "",
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
        View layout;

        if (position == 0) {
            layout = LayoutInflater.from(container.getContext()).inflate(R.layout.item_dragons, container, false);
        } else {
            layout = LayoutInflater.from(container.getContext()).inflate(R.layout.item_portfolio, container, false);
        }
        container.addView(layout);
        ImageView imageBackground = layout.findViewById(R.id.image_background);
        TextView textDonate = layout.findViewById(R.id.text_donate);

        int donationCode = 0;
        switch (position) {
            case 0:
                break;
            case 1:
                imageBackground.setImageResource(R.drawable.portfolio_donation_image);
                donationCode = Constants.DONATE_ADDING_PORTFOLIO;
                textDonate.setText(itemsName[position]);
                break;
            case 2:
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
                EventBus.getDefault().post(new OpenDragonsEvent());
            } else {
                DonateDialog.getInstance((Integer) v.getTag()).show(fragmentManager, DonateDialog.TAG);
            }

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
