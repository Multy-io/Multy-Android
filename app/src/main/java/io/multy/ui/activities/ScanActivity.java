/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;


public class ScanActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    private ZBarScannerView scannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        ButterKnife.bind(this);
        scannerView = new ZBarScannerView(this);
        setContentView(scannerView);
        Analytics.getInstance(this).logScanQRLaunch();
    }

    @Override
    public void onResume() {
        super.onResume();
        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QRCODE);
        scannerView.setFormats(formats);
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
        try {
            Uri uri = Uri.parse(rawResult.getContents().replace(",", "."));
            if (uri.getScheme() == null || uri.getQuery() == null) {
                addressIntent.putExtra(Constants.EXTRA_QR_CONTENTS, uri.getSchemeSpecificPart());
                return;
            }
            try {
                String[] queries = uri.getQuery().split("\\?:=|:=|=:|=|:");
                for (int i = 0; i < queries.length; i++) {
                    if (queries[i].toLowerCase().contains("amount")) {
                        Double.parseDouble(queries[i + 1]); //to check if amount is double
                        addressIntent.putExtra(Constants.EXTRA_AMOUNT, queries[i + 1]);
                        break;
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            addressIntent.putExtra(Constants.EXTRA_QR_CONTENTS, uri.getSchemeSpecificPart()
                    .substring(0, uri.getSchemeSpecificPart().indexOf(Constants.QUESTION_MARK)));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Analytics.getInstance(this).logScanQRClose();
        super.onBackPressed();
    }
}