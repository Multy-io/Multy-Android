/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.BindInt;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.util.Constants;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import timber.log.Timber;


public class ScanActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    @BindInt(R.integer.one)
    int one;
    @BindInt(R.integer.zero)
    int zero;

    private ZBarScannerView scannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        ButterKnife.bind(this);
        scannerView = new ZBarScannerView(this);
        setContentView(scannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Intent addressIntent = new Intent();
        parseUri(addressIntent, rawResult);

        setResult(RESULT_OK, addressIntent);
        finish();
    }

    private void parseUri(Intent addressIntent, Result rawResult){
        Uri uri = Uri.parse(rawResult.getContents());
        if (uri.getScheme() != null) { // was scanned scheme according to pattern
            String schemeSpecificPart = uri.getSchemeSpecificPart();
            if (schemeSpecificPart != null) {
                if (schemeSpecificPart.contains(Constants.QUESTION_MARK)) {
                    addressIntent.putExtra(Constants.EXTRA_QR_CONTENTS,
                            schemeSpecificPart.substring(zero, schemeSpecificPart.indexOf(Constants.QUESTION_MARK)));
                    if (schemeSpecificPart.indexOf(Constants.EQUAL) + one <= schemeSpecificPart.length()) {
                        Timber.e("amount54 %s", schemeSpecificPart.substring(schemeSpecificPart.indexOf(Constants.EQUAL) + one, schemeSpecificPart.length()));
                        addressIntent.putExtra(Constants.EXTRA_AMOUNT, schemeSpecificPart.substring(schemeSpecificPart.indexOf(Constants.EQUAL) + one, schemeSpecificPart.length()));
                    }
                } else {
                    addressIntent.putExtra(Constants.EXTRA_QR_CONTENTS, uri.getSchemeSpecificPart());
                }
            } else {
                addressIntent.putExtra(Constants.EXTRA_QR_CONTENTS, rawResult.getContents());
            }
        } else { // was scanned just address
            addressIntent.putExtra(Constants.EXTRA_QR_CONTENTS, rawResult.getContents());
        }
    }
}