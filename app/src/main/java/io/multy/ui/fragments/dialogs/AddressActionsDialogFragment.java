/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.asset.AssetInfoFragment;
import io.multy.ui.fragments.main.contacts.ContactsFragment;
import io.multy.util.Constants;
import io.multy.util.ContactUtils;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_SEND;

/**
 * Created by anschutz1927@gmail.com on 24.05.18.
 */
public class AddressActionsDialogFragment extends BottomSheetDialogFragment
        implements DialogInterface.OnShowListener {

    public static final String TAG = AddressActionsDialogFragment.class.getSimpleName();
    private static final int PICK_CONTACT_CODE = 560;
    private static final int PERMISSION_CODE = 561;
    private static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";
    private static final String EXTRA_CURRENCY_ID = "EXTRA_CURRENCY_ID";
    private static final String EXTRA_NETWORK_ID = "EXTRA_NETWORK_ID";
    private static final String EXTRA_RESOURCE_IMG_ID = "EXTRA_RESOURCE_IMG_ID";
    private static final String EXTRA_SHOW_CONTACT = "EXTRA_SHOW_CONTACT";

    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.image_qr)
    ImageView imageQr;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.button_add_to_contact)
    View buttonContact;

    private String address;
    private CompositeDisposable disposables;
    private int currencyId;
    private int networkId;
    private int resImgId;
    private Callback listener;

    public static AddressActionsDialogFragment getInstance(String address, int currencyId, int networkId,
                                                           int resImgId, boolean showContactItem) {
        return getInstance(address, currencyId, networkId, resImgId, showContactItem, null);
    }

    public static AddressActionsDialogFragment getInstance(String address, int currencyId, int networkId, int resImgId,
                                                           boolean showContactItem, Callback listener) {
        AddressActionsDialogFragment fragment = new AddressActionsDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ADDRESS, address);
        args.putInt(EXTRA_CURRENCY_ID, currencyId);
        args.putInt(EXTRA_NETWORK_ID, networkId);
        args.putInt(EXTRA_RESOURCE_IMG_ID, resImgId);
        args.putBoolean(EXTRA_SHOW_CONTACT, showContactItem);
        fragment.setArguments(args);
        fragment.setListener(listener);
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
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setBottomSheetCallback(getStateChangeListener());
            bottomSheet.setBackground(null);
        }
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_CODE && resultCode == RESULT_OK && getContext() != null) {
            Account account = getMultyAccount();
            if (getContext() != null && data.getData() != null && account != null) {
                Uri contactUri = data.getData();
                final Cursor contactCursor = getContext().getContentResolver()
                        .query(contactUri, null, null, null, null); //todo add projection
                if (contactCursor != null && contactCursor.moveToFirst()) {
                    String formattedAddress = ContactUtils.getFormattedAddressString(currencyId, address);
                    if (ContactUtils.isMultyLinkCreated(getContext(), contactCursor)) {
                        String rawId = ContactUtils.getMultyContactRawId(getContext(), contactCursor);
                        if (rawId != null) {
                            if (ContactUtils.isMultyAddressLinked(getContext(), formattedAddress)) {
                                Toast.makeText(getContext(), R.string.address_bind, Toast.LENGTH_SHORT).show();
                            } else {
                                ContactUtils.updateContact(getContext(), rawId, formattedAddress, currencyId, networkId, address);
                                ContactUtils.addAddressToLocalContact(Long.parseLong(rawId), address, currencyId, networkId, resImgId, () -> {
                                    if (listener != null) {
                                        listener.onComplete();
                                    }
                                });
                                Analytics.getInstance(getContext()).logContactAddressAdded();
                            }
                        } else {
                            Log.e(getClass().getSimpleName(), "The \"Multy link\" created, but we were not found contact in account!");
                        }
                    } else {
                        long parentRowId = contactCursor
                                .getLong(contactCursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
                        if (!ContactUtils.isMultyAddressLinked(getContext(), formattedAddress)) {
                            long multyRowId = ContactUtils.addContact(getContext(), contactCursor, String.valueOf(parentRowId),
                                    formattedAddress, currencyId, networkId, address);
                            if (multyRowId != -1) {
                                ContactUtils.createLocalContact(multyRowId, parentRowId, contactCursor, () ->
                                        ContactUtils.addAddressToLocalContact(multyRowId, address, currencyId, networkId, resImgId, () -> {
                                     if (listener != null) {
                                         listener.onComplete();
                                     }
                                }));
                            }
                            Analytics.getInstance(getContext()).logContactAdded();
                            Analytics.getInstance(getContext()).logContactAddressAdded();
                        } else {
                            long multyRowId = ContactUtils.addContact(getContext(), contactCursor, String.valueOf(parentRowId),
                                    null,null, null, null);
                            if (multyRowId != -1) {
                                ContactUtils.createLocalContact(multyRowId, parentRowId, contactCursor, () -> {});
                            }
                            Toast.makeText(getContext(), R.string.address_bind, Toast.LENGTH_SHORT).show();
                            Analytics.getInstance(getContext()).logContactAdded();
                        }
                    }
                    contactCursor.close();
                }
            }
            dismiss();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            requestContact();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private BottomSheetCallback getStateChangeListener() {
        return new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss();
                    if (listener != null) {
                        listener.onComplete();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
        };
    }

    private void initialize() {
        if (getArguments() == null) {
            dismiss();
            return;
        }
        this.address = getArguments().getString(EXTRA_ADDRESS);
        this.currencyId = getArguments().getInt(EXTRA_CURRENCY_ID);
        this.networkId = getArguments().getInt(EXTRA_NETWORK_ID);
        this.resImgId = getArguments().getInt(EXTRA_RESOURCE_IMG_ID);
        if (RealmManager.getSettingsDao().isAddressInContact(address) || !getArguments().getBoolean(EXTRA_SHOW_CONTACT)) {
            buttonContact.setVisibility(View.GONE);
        }
        textAddress.setText(address);
        textTitle.setText(String.format(getString(R.string.address_formatted), NativeDataHelper.Blockchain.valueOf(currencyId).name()));
        generateQr(address, getResources().getColor(android.R.color.black),
                getResources().getColor(android.R.color.white), qrBitmap -> {
            imageQr.setImageBitmap(qrBitmap);
            progressBar.setVisibility(View.GONE);
            }, throwable -> dismiss());
    }

    private void copyToClipboard() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_ADDRESS, currencyId);
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
            intentReceiver.putExtra(getString(R.string.chain_id), currencyId);
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

    private boolean checkPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : ContactsFragment.REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void requestContact() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        if (getActivity() != null && pickIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(pickIntent, PICK_CONTACT_CODE);
            Analytics.getInstance(getContext()).logContactPhoneBook();
        }
    }

    private Account getMultyAccount() {
        AccountManager accountManager = AccountManager.get(getContext());
        for (Account account : accountManager.getAccounts()) {
            if (account.name.equals(Constants.ACCOUNT_NAME) && account.type.equals(Constants.ACCOUNT_TYPE)) {
                return account;
            }
        }
        return createMultyAccount(accountManager);
    }

    private Account createMultyAccount(AccountManager accountManager) {
        Account account = new Account(Constants.ACCOUNT_NAME, Constants.ACCOUNT_TYPE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
//            ContentResolver.requestSync(account, ContactsContract.AUTHORITY, null);
            return account;
        }
        Log.e(this.getClass().getSimpleName(), "Error while creating Multy account!");
        return null;
    }

    public void setListener(Callback listener) {
        this.listener = listener;
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

    @OnClick(R.id.button_add_to_contact)
    void onClickToContact(View view) {
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);
        if (checkPermissions(view.getContext())) {
            requestContact();
        } else {
            requestPermissions(ContactsFragment.REQUIRED_PERMISSIONS, PERMISSION_CODE);
        }
    }

    public interface Callback {
        void onComplete();
    }
}
