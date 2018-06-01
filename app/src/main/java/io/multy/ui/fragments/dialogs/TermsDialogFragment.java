/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;

public class TermsDialogFragment extends DialogFragment {

    @BindView(R.id.button_accept)
    Button buttonPositive;

    @BindView(R.id.button_discard)
    Button buttonNegative;

    @BindView(R.id.web_view)
    WebView webView;

    private OnTermsInteractionListener listener;
    private String message;
    private String title;

    public interface OnTermsInteractionListener {
        void onAccepted();

        void onDiscarded();
    }


    public static TermsDialogFragment newInstance(OnTermsInteractionListener listener) {
        TermsDialogFragment simpleDialogFragment = new TermsDialogFragment();
        simpleDialogFragment.setListener(listener);
        return simpleDialogFragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
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

    private void setupWebView() {
        webView.setEnabled(false);
        webView.loadUrl(getString(R.string.terms_url));
    }

    private void showPrivacy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://raw.githubusercontent.com/wiki/Appscrunch/Multy/Legal:-Privacy-Policy.md"));
        startActivity(browserIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_agreement, container, false);
        ButterKnife.bind(this, view);
        setupWebView();
        return view;
    }

    @OnClick(R.id.button_accept)
    public void onClickPositive(View v) {
        if (listener != null) {
            listener.onAccepted();
        }
        dismiss();
    }

    @OnClick(R.id.button_discard)
    public void onClickNegative() {
        listener.onDiscarded();
        dismiss();
    }

    @OnClick(R.id.text_read)
    public void onClickPrivacy() {
        showPrivacy();
    }

    public OnTermsInteractionListener getListener() {
        return listener;
    }

    public void setListener(OnTermsInteractionListener listener) {
        this.listener = listener;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
