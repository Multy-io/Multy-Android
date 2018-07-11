/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main.contacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.Contact;
import io.multy.model.entities.ContactAddress;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.ContactAddressesAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.AddressActionsDialogFragment;
import io.multy.ui.fragments.dialogs.ContactDialog;
import io.multy.ui.fragments.send.AssetSendFragment;
import io.multy.util.Constants;
import io.multy.util.ContactUtils;
import io.multy.util.analytics.Analytics;
import io.multy.viewmodels.ContactsViewModel;

/**
 * Created by anschutz1927@gmail.com on 27.06.18.
 */
public class ContactInfoFragment extends BaseFragment implements ContactAddressesAdapter.OnClickListener {

    private static final String EXTRA_CONTACT_ID = "EXTRA_CONTACT_ID";

    @BindView(R.id.image_photo)
    ImageView imagePhoto;
    @BindView(R.id.text_name)
    TextView textName;
    @BindView(R.id.recycler_addresses)
    RecyclerView recyclerView;
    @BindView(R.id.button_add)
    View buttonAdd;

    private boolean parentStartVisibility;
    private long contactId;
    private Contact contact = null;
    private ContactAddressesAdapter addressesAdapter;
    private ContactsViewModel viewModel;

    public static ContactInfoFragment getInstance(long contactId) {
        ContactInfoFragment fragment = new ContactInfoFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_CONTACT_ID, contactId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addressesAdapter = new ContactAddressesAdapter(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_info, container, false);
        viewModel = ViewModelProviders.of(getActivity()).get(ContactsViewModel.class);
        ButterKnife.bind(this, view);
        parentStartVisibility = container == null || container.getVisibility() == View.VISIBLE;
        if (!parentStartVisibility) {
            container.setVisibility(View.VISIBLE);
        }
        initialize();
        return view;
    }

    @Override
    public void onResume() {
        if (contact != null && (!contact.isLoaded() || !contact.isValid() || !contact.isManaged()) && getActivity() != null) {
            getActivity().onBackPressed();
        }
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        if (!parentStartVisibility && getView() != null) {
            ((View) getView().getParent()).setVisibility(View.GONE);
        }
        super.onDestroyView();
    }

    @Override
    public void onClickAddress(ContactAddress contactAddress) {
        if (getActivity() != null && getTargetFragment() != null) {
            Intent intent = new Intent().putExtra(Constants.EXTRA_ADDRESS, contactAddress.getAddress());
            getActivity().onBackPressed();
            getActivity().onBackPressed();
            getTargetFragment().onActivityResult(AssetSendFragment.REQUEST_CONTACT, Activity.RESULT_OK, intent);
        } else {
            AddressActionsDialogFragment.getInstance(contactAddress.getAddress(), contactAddress.getCurrencyId(),
                    contactAddress.getNetworkId(), contactAddress.getCurrencyImgId(), false);
        }
        Analytics.getInstance(getContext()).logContactAddressSelected();
    }

    @Override
    public boolean onLongClickAddress(String address, int currencyId, int networkId) {
        if (getContext() != null && contact.isManaged() && contact.isValid()) {
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.delete)
                    .setMessage(R.string.shure_delete_address)
                    .setPositiveButton(R.string.yes, (dialog1, which) -> {
                        ContactUtils.deleteMultyAddress(getContext(), address, currencyId, networkId, this::notifyData);
                        dialog1.dismiss();
                        viewModel.setNotifyData(true);
                        Analytics.getInstance(getContext()).logContactAddressDeleted();
                    }).setNegativeButton(R.string.no, ((dialog1, which) -> dialog1.dismiss()))
                    .setCancelable(true)
                    .create();
            dialog.show();
        }
        return true;
    }

    private void initialize() {
        if (getArguments() != null) {
            contactId = getArguments().getLong(EXTRA_CONTACT_ID);
        }
        notifyData();
        textName.setText(contact.getName());
        if (!TextUtils.isEmpty(contact.getPhotoUri())) {
            imagePhoto.setImageURI(Uri.parse(contact.getPhotoUri()));
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(addressesAdapter);
        if (getTargetFragment() != null) {
            buttonAdd.setVisibility(View.GONE);
        }
    }

    private void notifyData() {
        contact = RealmManager.getSettingsDao().getContact(contactId);
        if (contact != null) {
            addressesAdapter.setData(contact.getAddresses());
        } else if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @OnClick(R.id.button_back)
    void onClickBack(View view) {
        view.setEnabled(false);
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @OnClick(R.id.button_add)
    void onClickAdd(View view) {
        if (getActivity() != null) {
            ContactDialog.getInstance(contactId, this::notifyData).show(getChildFragmentManager(), ContactDialog.TAG);
        }
    }
}
