/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.guilhe.circularprogressview.CircularProgressView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.Owner;

public class CreateMultiSigActivity extends BaseActivity {

    @BindView(R.id.progress)
    CircularProgressView circularProgressView;
    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.text_status)
    TextView textStatus;
    @BindView(R.id.text_action)
    TextView textAction;
    @BindView(R.id.image_action)
    ImageView imageAction;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_multisig);

    }

    private ArrayList<Owner> getOwners() {
        ArrayList<Owner> owners = new ArrayList<>();
        owners.add(new Owner("John Kek", "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2"));
        owners.add(new Owner("Tong Ji Mao", "3J98t1WpEZ73CNmQviecrnyiWrnqRhWNLy"));
        owners.add(new Owner("Dick Plane", "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq"));

        return owners;
    }

    @OnClick(R.id.button_action)
    public void onClickAction() {

    }
}
