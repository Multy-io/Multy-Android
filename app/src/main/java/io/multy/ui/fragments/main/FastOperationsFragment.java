/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.activities.FastReceiveActivity;
import io.multy.ui.activities.MainActivity;
import io.multy.ui.activities.TestOperationsActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.AnimationUtils;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.realm.RealmResults;

/**
 * Created by Ihar Paliashchuk on 02.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class FastOperationsFragment extends BaseFragment {

    public static final String TAG = FastOperationsFragment.class.getSimpleName();

    @BindView(R.id.button_cancel)
    View buttonCancel;

    @BindView(R.id.container)
    View container;

    @BindColor(R.color.colorPrimary)
    int colorBlue;

    @BindColor(R.color.white)
    int colorWhite;

    private int revealX;
    private int revealY;
    private boolean isCanceling = false;

    public static FastOperationsFragment newInstance(int revealX, int revealY) {
        FastOperationsFragment fragment = new FastOperationsFragment();
        fragment.setRevealX(revealX);
        fragment.setRevealY(revealY);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVisibilityContainer(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fast_operations, container, false);
        ButterKnife.bind(this, view);
        Analytics.getInstance(getActivity()).logFastOperationsLaunch();
        AnimationUtils.createReveal(view, revealX, revealY, colorBlue, colorWhite);
        buttonCancel.setEnabled(false);
        buttonCancel.postDelayed(() -> buttonCancel.setEnabled(true), AnimationUtils.DURATION_MEDIUM);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setVisibilityContainer(View.GONE);
    }

    private void setVisibilityContainer(int visibility) {
        View fullContainer = getActivity().findViewById(R.id.full_container);
        if (fullContainer != null) {
            fullContainer.setVisibility(visibility);
        }
    }

    private boolean isWalletsAvailable() {
        RealmResults<Wallet> wallets = RealmManager.getAssetsDao().getWallets();
        return wallets != null && wallets.size() > 0;
    }

    @OnClick(R.id.button_send)
    void onSendClick() {
        Analytics.getInstance(getActivity()).logFastOperations(AnalyticsConstants.FAST_OPERATIONS_SEND);
        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED) && isWalletsAvailable()) {
//            startActivity(new Intent(getContext(), AssetSendActivity.class));
            startActivity(new Intent(getActivity(), TestOperationsActivity.class));
        } else {
            Toast.makeText(getActivity(), "Please, create wallet", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.button_receive)
    void onReceiveClick() {
        Analytics.getInstance(getActivity()).logFastOperations(AnalyticsConstants.FAST_OPERATIONS_RECEIVE);
        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED) && isWalletsAvailable()) {
            startActivity(new Intent(getContext(), AssetRequestActivity.class));
        } else {
            Toast.makeText(getActivity(), "Please, create wallet", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.button_nfc)
    void onNfcClick() {
        Analytics.getInstance(getActivity()).logFastOperations(AnalyticsConstants.FAST_OPERATIONS_NFC);
        Toast.makeText(getActivity(), R.string.not_implemented, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_scan_qr)
    void onScanClick() {
        Analytics.getInstance(getActivity()).logFastOperations(AnalyticsConstants.FAST_OPERATIONS_SCAN);
        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED) && isWalletsAvailable()) {
            ((MainActivity) getActivity()).showScanScreen();
        } else {
            Toast.makeText(getActivity(), "Please, create wallet", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.button_cancel)
    void onCancelClick(View v) {
        Analytics.getInstance(getActivity()).logFastOperations(AnalyticsConstants.FAST_OPERATIONS_CLOSE);
        isCanceling = true;
        if (v != null) {
            AnimationUtils.createConceal(getView(), revealX, revealY, colorWhite, colorBlue, () -> getActivity().onBackPressed());
            v.setEnabled(false);
        } else {
            getActivity().onBackPressed();
        }
    }

    public void cancel() {
        if (!isCanceling) {
            onCancelClick(null);
            buttonCancel.setEnabled(false);
        }
    }

    public boolean isCanceling() {
        return isCanceling;
    }

    public void setRevealX(int revealX) {
        this.revealX = revealX;
    }

    public void setRevealY(int revealY) {
        this.revealY = revealY;
    }
}
