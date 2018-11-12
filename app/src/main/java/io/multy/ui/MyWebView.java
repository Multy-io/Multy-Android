/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import timber.log.Timber;
import trust.web3.Web3View;

public class MyWebView extends Web3View {

    public MyWebView(@NonNull Context context) {
        super(context);
        setWebViewClient();
    }

    public MyWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWebViewClient();
    }

    public MyWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWebViewClient();
    }

    private void enableHTML5AppCache() {
//        getSettings().setDomStorageEnabled(true);
//
//        // Set cache size to 8 mb by default. should be more than enough
//        getSettings().setAppCacheMaxSize(1024 * 1024 * 8);

        // This next one is crazy. It's the DEFAULT location for your app's cache
        // But it didn't work for me without this line
        getSettings().setAppCachePath("/data/data/" + getContext().getPackageName() + "/cache");
        getSettings().setAllowFileAccess(true);
        getSettings().setAppCacheEnabled(true);
        getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    boolean setWebViewClient() {
        setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus(View.FOCUS_DOWN);

        setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    if (!v.hasFocus()) {
                        v.requestFocus();
                    }
                    break;
            }
            return false;
        });

        this.setWebViewClient(new WebViewClient() {
            ProgressDialog dialog = new ProgressDialog(getContext());

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                loadUrl(url);
                Timber.v("URL=" + url);
//                loadData(url, "text/html", "UTF-8");
                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Timber.e("ERROR " + errorCode + " " + description);
                Toast.makeText(getContext(), description, Toast.LENGTH_SHORT).show();
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (dialog != null) {
                    dialog.setMessage("Loading");
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(true);
                    dialog.show();
                }

            }

            public void onPageFinished(WebView view, String url) {
                if (dialog != null) {
                    dialog.cancel();
                }
            }
        });

        this.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Timber.i("JS Alert " + message + " " + result.toString());
                result.confirm();
                return true;
            }
        });

        enableHTML5AppCache();
        return true;
    }
}