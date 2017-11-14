/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;

import butterknife.ButterKnife;
import io.multy.R;

/**
 * Created by Ihar Paliashchuk on 03.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class AssetSendActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);
        ButterKnife.bind(this);
    }

}
