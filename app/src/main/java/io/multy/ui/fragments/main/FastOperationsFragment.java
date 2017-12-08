/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.FeedViewModel;

/**
 * Created by Ihar Paliashchuk on 02.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class FastOperationsFragment extends BaseFragment {

    public static final String TAG = FastOperationsFragment.class.getSimpleName();

    private FeedViewModel viewModel;

    public static FastOperationsFragment newInstance(){
        return new FastOperationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVisibilityContainer(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        DataBindingUtil.inflate(inflater, R.layout.fragment_feed, container, false);
        View view = inflater.inflate(R.layout.fragment_fast_operations, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(this).get(FeedViewModel.class);
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

    @OnClick(R.id.button_send)
    void onSendClick() {
        startActivity(new Intent(getContext(), AssetSendActivity.class));
    }

    @OnClick(R.id.button_receive)
    void onReceiveClick() {
        startActivity(new Intent(getContext(), AssetRequestActivity.class));
    }

    @OnClick(R.id.button_nfc)
    void onNfcClick() {
    }

    @OnClick(R.id.button_scan_qr)
    void onScanClick(){
    }

    @OnClick(R.id.button_cancel)
    void onCancelClick() {
        getActivity().onBackPressed();
    }
}
