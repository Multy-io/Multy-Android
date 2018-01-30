/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;

/**
 * Created by anschutz1927@gmail.com on 22.01.18.
 */

public class WebFragment extends BaseFragment {

    public static final String TAG = WebFragment.class.getSimpleName();

    @BindView(R.id.web)
    WebView webView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout refresh;

    private String url = "http://multy.io/";

    public static WebFragment newInstance(String url) {
        WebFragment webFragment = new WebFragment();
        webFragment.setUrl(url);
        return webFragment;
    }

    public WebFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);
        ButterKnife.bind(this, view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                refresh.setRefreshing(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                refresh.setRefreshing(false);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Toast.makeText(getContext(), R.string.failed_html_page, Toast.LENGTH_SHORT).show();
            }
        });
        webView.loadUrl(url);
        refresh.setOnRefreshListener(() -> {
            webView.reload();
        });
        return view;
    }

    private void setUrl (String url) {
        this.url = url;
    }
}
