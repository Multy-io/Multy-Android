/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.send.SendSummaryFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;

/**
 * Created by anschutz1927@gmail.com on 22.01.18.
 */

public class WebDialogFragment extends DialogFragment {

    public static final String TAG = WebDialogFragment.class.getSimpleName();

    @BindView(R.id.web)
    WebView webView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout refresh;

    private String url = "http://multy.io/donation_features";

    public static WebDialogFragment newInstance(String url) {
        WebDialogFragment webFragment = new WebDialogFragment();
        webFragment.setUrl(url);
        return webFragment;
    }

    public WebDialogFragment() {}

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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (getTag() != null && getTag().equals(SendSummaryFragment.TAG_SEND_SUCCESS)) {
            if (getArguments() != null) {
                Analytics.getInstance(getActivity()).logSendSuccessLaunch(getArguments().getInt(Constants.CHAIN_ID));
            }
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
