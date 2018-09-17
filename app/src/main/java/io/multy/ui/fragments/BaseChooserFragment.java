/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;

/**
 * Created by anschutz1927@gmail.com on 05.03.18.
 */

public class BaseChooserFragment extends BaseFragment {

    @BindView(R.id.recycler_available)
    RecyclerView availableRecyclerView;
    @BindView(R.id.recycler_soon)
    RecyclerView soonRecyclerView;
    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.group_soon)
    Group groupSoon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_choose_available_soon, container, false);
        ButterKnife.bind(this, v);
        availableRecyclerView.setNestedScrollingEnabled(false);
        soonRecyclerView.setNestedScrollingEnabled(false);
        return v;
    }

    protected RecyclerView getBlockAvailableRecyclerView() {
        return availableRecyclerView;
    }

    protected RecyclerView getBlockSoonRecyclerView() {
        return soonRecyclerView;
    }

    protected void setSoonGroupVisibility(int visibility) {
        groupSoon.setVisibility(visibility);
    }

    protected void setTitle(int stringTitleId) {
        textTitle.setText(stringTitleId);
    }

    @OnClick(R.id.button_back)
    void onBackClick(View view) {
        view.setEnabled(false);
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}
