/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.FastReceiver;
import io.multy.util.CryptoFormatUtils;

public class FastReceiverFragment extends Fragment {

    @BindView(R.id.circle)
    ImageView circleView;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.root)
    View rootView;
    @BindView(R.id.text_amount)
    TextView textAmount;
    @BindView(R.id.text_address)
    TextView textAddress;

    private FastReceiver receiver;

    public static FastReceiverFragment newInstance() {
        Bundle args = new Bundle();
        FastReceiverFragment fastReceiverFragment = new FastReceiverFragment();
        fastReceiverFragment.setArguments(args);
        return fastReceiverFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup convertView = (ViewGroup) inflater.inflate(R.layout.fragment_fast_receiver, container, false);
        ButterKnife.bind(this, convertView);

        if (receiver != null) {
            textAddress.setText(receiver.getAddress());
            final long amountSatoshi = Long.parseLong(receiver.getAmount());
            textAmount.setText(CryptoFormatUtils.satoshiToBtcLabel(amountSatoshi));
            circleView.setImageResource(FastReceiver.getImageResId(receiver.getAddress()));
        }
        return convertView;
    }

    public void setReceiver(FastReceiver receiver) {
        this.receiver = receiver;
    }

    public void animateSuccess() {
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();
    }

    public void hideRight() {
        rootView.animate().translationXBy(rootView.getWidth()).setDuration(100).start();
    }

    public void hideLeft() {
        rootView.animate().translationXBy(-rootView.getWidth()).setDuration(100).start();
    }

    public void showRight() {
        rootView.animate().translationXBy(-rootView.getWidth()).setDuration(100).start();
    }

    public void showLeft() {
        rootView.animate().translationXBy(rootView.getWidth()).setDuration(100).start();
    }
}
