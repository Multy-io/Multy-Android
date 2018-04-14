/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;

public class AgreementActivity extends AppCompatActivity {

    @BindView(R.id.web_view)
    WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        ButterKnife.bind(this);
        setupWebView();
    }

    private void setupWebView() {
        webView.setEnabled(false);
        webView.loadUrl("https://raw.githubusercontent.com/wiki/Appscrunch/Multy/Legal:-Terms-of-service.md");
    }

    @OnClick(R.id.button_discard)
    public void onClickDiscard() {
        onBackPressed();
    }

    @OnClick(R.id.button_accept)
    public void onClickAccept() {
        onBackPressed();
    }
}
