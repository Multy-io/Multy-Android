/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.content.Intent.ACTION_SEND;

/**
 * Created by anschutz1927@gmail.com on 30.08.18.
 */
public class ShareMultisigFragment extends BaseFragment {

    private static final String INVITATION_CODE = "INVITATION_CODE";

    private CompositeDisposable disposables;

    @BindView(R.id.text_key)
    TextView textKey;
    @BindView(R.id.image_qr)
    ImageView imageQr;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.group_key)
    Group groupKey;

    public static ShareMultisigFragment newInstance(String invitationCode) {
        ShareMultisigFragment fragment = new ShareMultisigFragment();
        Bundle args = new Bundle();
        args.putString(INVITATION_CODE, invitationCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposables = new CompositeDisposable();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_multisig, container, false);
        ButterKnife.bind(this, view);
        textKey.setText(getArguments() != null ? getArguments().getString(INVITATION_CODE) : null);
        groupKey.setVisibility(View.GONE);
        generateQr();
        return view;
    }

    @Override
    public void onDestroyView() {
        if (!disposables.isDisposed()) {
            disposables.dispose();
        }
        super.onDestroyView();
    }

    private void generateQr() {
        generateQr(getArguments() != null ? "invite code: " + getArguments().getString(INVITATION_CODE, "") : "",
                getResources().getColor(android.R.color.black), getResources().getColor(android.R.color.white),
                bitmap -> {
            imageQr.setImageBitmap(bitmap);
            progress.setVisibility(View.GONE);
            groupKey.setVisibility(View.VISIBLE);
                }, Throwable::printStackTrace);
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
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 0);
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(strQr, BarcodeFormat.QR_CODE, 200, 200, hints);
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

    private void share() {
        String key = getArguments() != null ? getArguments().getString(INVITATION_CODE, "") : "";
        if (!TextUtils.isEmpty(key)) {
            Intent sharingIntent = new Intent(ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, key);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share)));
        }
    }

    @OnClick(R.id.button_share)
    void onClickShare(View view) {
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);
        share();
    }

    @OnClick(R.id.button_close)
    void onClickClose(View view) {
        if (getActivity() != null) {
            view.setEnabled(false);
            getActivity().onBackPressed();
        }
    }
}
