/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.multy.R;
import io.multy.api.socket.BlueSocketManager;
import io.multy.model.entities.wallet.MultisigEvent;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.CreateMultiSigActivity;
import io.multy.ui.adapters.MyWalletsAdapter;
import io.multy.ui.fragments.dialogs.WalletChooserDialogFragment;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.realm.Realm;
import io.socket.client.Ack;
import io.socket.client.Socket;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import timber.log.Timber;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

/**
 * Created by anschutz1927@gmail.com on 23.07.18.
 */
public class ScanInvitationCodeFragment extends BaseFragment {

    public static final String TAG = ScanInvitationCodeFragment.class.getSimpleName();

    @BindView(R.id.image_camera)
    ZBarScannerView scanner;
    @BindView(R.id.input_code)
    EditText inputCode;
    @BindView(R.id.button_join)
    View buttonJoin;
    @BindView(R.id.top_background)
    View backgroundTop;
    @BindView(R.id.bottom_background)
    View backgroundBottom;
    @BindView(R.id.background)
    View background;
    @BindView(R.id.container)
    View container;
    @BindColor(R.color.background_dark_transparent)
    int colorDark;

    public static ScanInvitationCodeFragment newInstance() {
        return new ScanInvitationCodeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN | SOFT_INPUT_ADJUST_RESIZE);
        }
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_invite_code, container, false);
        ButterKnife.bind(this, view);
        initialize();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (permissionsGranted(getContext())) {
            scanner.startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onPause() {
        scanner.stopCamera();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        container.removeOnLayoutChangeListener(this::onParentLayoutChange);
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.CAMERA_REQUEST_CODE && getActivity() != null) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    getActivity().onBackPressed();
                    break;
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WalletChooserDialogFragment.REQUEST_WALLET_ID && resultCode == Activity.RESULT_OK && getActivity() != null) {
            long walletId = data.getLongExtra(Constants.EXTRA_WALLET_ID, 0);
//            startActivity(new Intent(getContext(), CreateMultiSigActivity.class).putExtra(Constants.EXTRA_WALLET_ID, walletId));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initialize() {
        scanner.setFormats(Collections.singletonList(BarcodeFormat.QRCODE));
        scanner.setResultHandler(this::onResult);
        scanner.setAutoFocus(true);
        container.addOnLayoutChangeListener(this::onParentLayoutChange);
        buttonJoin.setVisibility(View.GONE);
    }

    private boolean permissionsGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void onParentLayoutChange(View v, int left, int top, int right, int bottom,
                                      int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int minSize = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
        setBackgroundDarkness((bottom - top) < minSize);
    }

    private void onResult(Result result) {
        try {
            String resultString = result.getContents();
            resultString = resultString.replace("invite code: ", "");
            if (resultString == null || resultString.length() != Constants.INVITE_CODE_LENGTH) {
                throw new IllegalArgumentException("Scanned data has not have invite code.");
            } else {
                inputCode.setText(resultString);
            }
        } catch (Exception e) {
            e.printStackTrace();
            scanner.resumeCameraPreview(this::onResult);
        }
        scanner.postDelayed(() -> scanner.resumeCameraPreview(this::onResult), 3000);
    }

    private void setBackgroundDarkness(boolean isDark) {
        backgroundTop.setVisibility(isDark ? View.GONE : View.VISIBLE);
        backgroundBottom.setVisibility(isDark ? View.GONE : View.VISIBLE);
        background.setVisibility(isDark ? View.VISIBLE : View.GONE);
    }

    @OnTextChanged(value = R.id.input_code, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onTextChanged(Editable editable) {
        if (editable.toString().contains(" ") || editable.toString().contains("\n")) {
            String editedString = editable.toString().replaceAll("[ ,\n]", "");
            editable.clear();
            editable.append(editedString);
        }
        if (editable.length() > 0) {
            buttonJoin.setVisibility(View.VISIBLE);
            if (editable.length() == 45) {
                inputCode.setEnabled(false);
                inputCode.postDelayed(() -> {
                    buttonJoin.performClick();
                    inputCode.setEnabled(true);
                }, 300);
            } else if (buttonJoin.isEnabled()) {
                buttonJoin.setEnabled(false);
            }
        } else {
            buttonJoin.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.button_close)
    void onClickClose() {
        getActivity().onBackPressed();
    }

    @OnClick(R.id.button_join)
    void onClickJoin(View view) {
        Timber.i("CLICKED JOIN");

        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);

        final String inviteCode = inputCode.getText().toString();
        CreateMultiSigActivity activity = ((CreateMultiSigActivity) getActivity());

        Socket socket = activity.getSocket();
        final String userId = RealmManager.getSettingsDao().getUserId().getUserId();
        final String endpoint = "message:send";
        MultisigEvent.Payload payload = new MultisigEvent.Payload();
        payload.inviteCode = inviteCode;
        payload.userId = userId;

        MultisigEvent event = new MultisigEvent(CreateMultiSigActivity.SOCKET_VALIDATE, System.currentTimeMillis(), payload);

        try {
            Timber.i("sending socket " + new JSONObject(new Gson().toJson(event)).toString());
            socket.emit(endpoint, new JSONObject(new Gson().toJson(event)), new Ack() {
                @Override
                public void call(Object... args) {
                    Timber.i("VALIDATE: " + args[0].toString());

                    MultisigEvent responseEvent = new Gson().fromJson(args[0].toString(), MultisigEvent.class);
                    if (responseEvent.payload.exist) {
                        if (getFragmentManager() != null) {

                            WalletChooserDialogFragment dialog = WalletChooserDialogFragment.getInstance(NativeDataHelper.Blockchain.ETH.getValue());
                            dialog.setTargetFragment(ScanInvitationCodeFragment.this, WalletChooserDialogFragment.REQUEST_WALLET_ID);
                            dialog.setOnWalletClickListener(wallet -> {

                                activity.setConnectedWallet(wallet);
                                activity.setInviteCode(inputCode.getText().toString());
                                dialog.dismiss();
                                activity.updateInfo();
                                activity.getSupportFragmentManager().beginTransaction().remove(ScanInvitationCodeFragment.this).commit();
                            });
                            dialog.show(getFragmentManager(), WalletChooserDialogFragment.TAG);
                        }
                    } else {
                        Toast.makeText(getActivity(), R.string.invite_code_not_recognized, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
