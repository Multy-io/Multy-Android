/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.adapters.AddressChainAdapter;
import io.multy.util.ContactUtils;
import io.multy.util.NativeDataHelper;

/**
 * Created by anschutz1927@gmail.com on 06.07.18.
 */
public class ContactDialog extends BottomSheetDialogFragment implements DialogInterface.OnShowListener {

    public static final String TAG = ContactDialog.class.getSimpleName();

    @BindView(R.id.input_address)
    TextInputEditText inputAddress;
    @BindView(R.id.spinner_chain)
    Spinner spinner;
    @BindArray(R.array.available_chain_image_ids)
    TypedArray chainImageIds;
    @BindArray(R.array.available_chain_abbrev)
    String[] chainAbbrevs;
    @BindArray(R.array.available_chain_name)
    String[] chainNames;
    @BindArray(R.array.available_chain_net_types)
    int[] chainNets;
    @BindArray(R.array.available_chain_ids)
    int[] chainIds;

    private Callback callback;
    private AddressChainAdapter chainAdapter;
    private long contactId;

    public static ContactDialog getInstance(long rawContactId, Callback callback) {
        ContactDialog fragment = new ContactDialog();
        fragment.setCallback(callback);
        fragment.setContactId(rawContactId);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ActionsBottomSheetDialog);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.bottom_sheet_contact_dialog, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);
        dialog.setOnShowListener(this);
        initialize(getContext());
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

    private BottomSheetBehavior.BottomSheetCallback getStateChangeListener() {
        return new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss();
                    if (callback != null) {
                        callback.onComplete();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
        };
    }

    private void initialize(Context context) {
        if (chainAdapter == null) {
            chainAdapter = new AddressChainAdapter(context, R.layout.item_chain_availabe);
        }
        chainAdapter.setData(chainImageIds, chainAbbrevs, chainNames, chainIds, chainNets);
        spinner.setAdapter(chainAdapter);
    }

    private boolean isValidData(@Nullable String address, int currencyId, int networkId) {
        if (TextUtils.isEmpty(address)) {
            return false;
        }
        return checkAddressForValidation(address, currencyId, networkId);
    }

    private boolean checkAddressForValidation(String address, int blockchainId, int networkId) {
        try {
            NativeDataHelper.isValidAddress(address, blockchainId, networkId);
            return true;
        } catch (Throwable ignore) { }
        return false;
    }

    private void addAddressToContact(Context context, String address, int currencyId, int networkId, int imgId) {
        String formattedAddress = ContactUtils.getFormattedAddressString(currencyId, address);
        if (!ContactUtils.isMultyAddressLinked(context, formattedAddress)) {
            ContactUtils.updateContact(context, String.valueOf(contactId), formattedAddress, currencyId, networkId, address);
            ContactUtils.addAddressToLocalContact(contactId, address, currencyId, networkId, imgId, () -> {
                if (callback != null) {
                    callback.onComplete();
                }
                dismiss();
            });
        } else {
            Toast.makeText(context, R.string.address_bind, Toast.LENGTH_SHORT).show();
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    @OnClick(R.id.button_ok)
    void onClickOk(View view) {
        if (chainAdapter != null) {
            final int position = spinner.getSelectedItemPosition();
            final int chainId = chainAdapter.getChainId(position);
            final int netId = chainAdapter.getChainNet(position);
            final int imgId = chainAdapter.getImgId(position);
            final String address = inputAddress.getText().toString();
            if (isValidData(address, chainId, netId)) {
                addAddressToContact(view.getContext(), address, chainId, netId, imgId);
            } else {
                Toast.makeText(getContext(), R.string.address_not_match, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.button_cancel)
    void onClickCancel() {
        if (getDialog() != null) {
            getDialog().dismiss();
        }
    }

    public interface Callback {
        void onComplete();
    }
}
