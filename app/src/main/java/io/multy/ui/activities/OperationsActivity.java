/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import butterknife.ButterKnife;
import io.multy.R;

/**
 * Created by andre on 13.11.2017.
 */

public class OperationsActivity extends BaseActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_operations);
        ButterKnife.bind(this);
    }

}
