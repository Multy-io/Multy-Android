/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.ui.fragments.asset.AssetInfoFragment;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.content.Intent.ACTION_SEND;

/**
 * Created by anschutz1927@gmail.com on 24.05.18.
 */
public class AddressActionsDialogFragment extends BottomSheetDialogFragment implements DialogInterface.OnShowListener {

    public static final String TAG = AddressActionsDialogFragment.class.getSimpleName();

    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.image_qr)
    ImageView imageQr;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.text_address)
    TextView textAddress;

    private Wallet wallet;
    private String address;
    private CompositeDisposable disposables;

    public static AddressActionsDialogFragment getInstance(Wallet wallet, String address) {
        AddressActionsDialogFragment fragment = new AddressActionsDialogFragment();
        fragment.setData(wallet, address);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        disposables = new CompositeDisposable();
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ActionsBottomSheetDialog);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.bottom_sheet_address_actions, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);
        dialog.setOnShowListener(this);
        initialize();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        FrameLayout bottomSheet = ((BottomSheetDialog) dialog)
                .findViewById(android.support.design.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheet.setBackground(null);
        }
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
        super.onDestroy();
    }

    private void initialize() {
        if (wallet == null || address == null) {
            dismiss();
            return;
        }
        textAddress.setText(address);
        textTitle.setText(String.format(getString(R.string.address_formatted), wallet.getCurrencyName()));
        generateQr(address, getResources().getColor(android.R.color.black),
                getResources().getColor(android.R.color.white), qrBitmap -> {
            imageQr.setImageBitmap(qrBitmap);
            progressBar.setVisibility(View.GONE);
            }, throwable -> dismiss());
    }

    private void copyToClipboard() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_ADDRESS, wallet.getCurrencyId());
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(address, address);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), R.string.address_copied, Toast.LENGTH_SHORT).show();
    }

    private void share() {
        Intent sharingIntent = new Intent(ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, address);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Intent intentReceiver = new Intent(getActivity(), AssetInfoFragment.SharingBroadcastReceiver.class);
            intentReceiver.putExtra(getString(R.string.chain_id), wallet.getCurrencyId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intentReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share), pendingIntent.getIntentSender()));
        } else {
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share)));
        }
    }

    private void generateQr(String strQr, int colorDark, int colorLight,
                           Consumer<Bitmap> consumerNext, Consumer<Throwable> consumerError) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Bitmap>) e -> {
            try {
                Bitmap bitmap = generateQr(strQr, colorDark, colorLight);
                e.onNext(bitmap);
            } catch (Throwable throwable) {
                e.onError(throwable);
            }
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumerNext, consumerError);
        disposables.add(disposable);
    }

    private Bitmap generateQr(String strQr, int colorDark, int colorLight) throws Throwable {
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(strQr, BarcodeFormat.QR_CODE, 200, 200, null);
        final int bitMatrixWidth = bitMatrix.getWidth();
        final int bitMatrixHeight = bitMatrix.getHeight();
        final int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];
        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? colorDark : colorLight;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, bitMatrixWidth, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    private void setData(Wallet wallet, String address) {
        this.wallet = wallet;
        this.address = address;
    }

    @OnClick({R.id.button_copy, R.id.button_share, R.id.button_cancel})
    void onClickButton(View view) {
        switch (view.getId()) {
            case R.id.button_copy:
                copyToClipboard();
                break;
            case R.id.button_share:
                share();
                break;
        }
        this.dismiss();
    }
}
