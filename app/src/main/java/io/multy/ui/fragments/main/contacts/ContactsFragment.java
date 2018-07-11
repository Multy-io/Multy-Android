/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main.contacts;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.adapters.ContactAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.send.AssetSendFragment;
import io.multy.util.Constants;
import io.multy.util.ContactUtils;
import io.multy.util.analytics.Analytics;
import io.multy.viewmodels.ContactsViewModel;


public class ContactsFragment extends BaseFragment implements ContactAdapter.OnClickListener {

    public static final String TAG = ContactsFragment.class.getSimpleName();
    public static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_SYNC_SETTINGS,
            Manifest.permission.WRITE_SYNC_SETTINGS
    };
    private static final int PERMISSION_CODE = 1101;
    private static final int PICK_CONTACT_CODE = 1102;

    @BindView(R.id.group_notification)
    Group groupNotification;
    @BindView(R.id.button_warn)
    View buttonWarn;
    @BindView(R.id.recycler_contact)
    RecyclerView recyclerView;
    @BindView(R.id.button_add)
    View buttonAdd;
    @BindView(R.id.button_back)
    View buttonBack;

    private ContactsViewModel viewModel;
    private ContactAdapter contactAdapter;
    private Account account;

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        viewModel = ViewModelProviders.of(getActivity()).get(ContactsViewModel.class);
        ButterKnife.bind(this, view);
        Analytics.getInstance(getActivity()).logContactsLaunch();
        if (!checkPermissions(getContext())) {
            buttonWarn.setVisibility(View.VISIBLE);
            groupNotification.setVisibility(View.VISIBLE);
            requestPermissions();
        }
        init(getContext());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        contactAdapter.notifyData();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        contactAdapter.removeRealmListener();
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ContactsFragment.PICK_CONTACT_CODE && resultCode == Activity.RESULT_OK) {
            if (getContext() != null && data.getData() != null && (account != null || initContactAccount())) {
                Uri contactUri = data.getData();
                Cursor contactCursor = getContext().getContentResolver()
                        .query(contactUri, null, null, null, null); //todo add projection
                if (contactCursor != null && contactCursor.moveToFirst()) {
                    ContactUtils.logCursor(contactCursor);
                    if (!ContactUtils.isMultyLinkCreated(getContext(), contactCursor)) {
                        long parentRowId = contactCursor
                                .getLong(contactCursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
                        long multyRowId = ContactUtils.addContact(getContext(), contactCursor, String.valueOf(parentRowId),
                                null, null, null, null);
                        if (multyRowId != -1) {
                            ContactUtils.createLocalContact(multyRowId, parentRowId, contactCursor, () -> {
                                contactAdapter.notifyData();
                                checkNotificationVisibility();});
                        }
                    }
                    contactCursor.close();
                }
            }
            Analytics.getInstance(getContext()).logContactAdded();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ContactsFragment.PERMISSION_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    buttonWarn.setVisibility(View.VISIBLE);
                    return;
                }
            }
            buttonWarn.setVisibility(View.GONE);
            init(getContext());
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClickContact(long contactId) {
        if (getActivity() != null && getView() != null) {
            ContactInfoFragment fragment = ContactInfoFragment.getInstance(contactId);
            if (getTargetFragment() != null) {
                fragment.setTargetFragment(getTargetFragment(), AssetSendFragment.REQUEST_CONTACT);
            }
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(((ViewGroup) getView().getParent()).getId(), fragment)
                    .addToBackStack(getClass().getSimpleName())
                    .commit();
        }
    }

    @Override
    public boolean onLongClickContact(long contactId) {
        if (getContext() != null && getTargetFragment() == null) {
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.delete)
                    .setMessage(R.string.shure_delete_contact)
                    .setPositiveButton(R.string.yes, (dialog1, which) -> {
                        ContactUtils.deleteMultyContact(getContext(), contactId, () -> {
                            contactAdapter.notifyData();
                            checkNotificationVisibility();
                            Analytics.getInstance(getContext()).logContactDeleted();
                        });
                        dialog1.dismiss();
                    }).setNegativeButton(R.string.no, ((dialog1, which) -> dialog1.dismiss()))
                    .setCancelable(true)
                    .create();
            dialog.show();
        }
        return true;
    }

    private boolean initContactAccount() {
        AccountManager accountManager = AccountManager.get(getContext());
        for (Account account : accountManager.getAccounts()) {
            if (account.name.equals(Constants.ACCOUNT_NAME) && account.type.equals(Constants.ACCOUNT_TYPE)) {
                this.account = account;
                return true;
            }
        }
        return createMultyAccount(accountManager);
    }

    private boolean createMultyAccount(AccountManager accountManager) {
        Account account = new Account(Constants.ACCOUNT_NAME, Constants.ACCOUNT_TYPE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
//            ContentResolver.requestSync(account, ContactsContract.AUTHORITY, null);
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
            this.account = account;
            return true;
        }
        Log.e(this.getClass().getSimpleName(), "Error while creating Multy account!");
        return false;
    }

    private boolean checkPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void requestPermissions() {
        requestPermissions(REQUIRED_PERMISSIONS, ContactsFragment.PERMISSION_CODE);
    }

    private void init(Context context) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        contactAdapter = new ContactAdapter(this);
        contactAdapter.notifyData();
        recyclerView.setAdapter(contactAdapter);
        checkNotificationVisibility();
        viewModel.getNotifyData().observe(this, b -> contactAdapter.notifyData());
        if (getTargetFragment() != null) {
            buttonAdd.setVisibility(View.GONE);
            buttonBack.setVisibility(View.VISIBLE);
        }
    }

    private void checkNotificationVisibility() {
        groupNotification.setVisibility(recyclerView.getAdapter().getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.button_add)
    void onClickAddContact(View v) {
        v.setEnabled(false);
        v.postDelayed(() -> v.setEnabled(true), 500);
        if (checkPermissions(v.getContext())) {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            if (getActivity() != null && pickIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(pickIntent, ContactsFragment.PICK_CONTACT_CODE);
                Analytics.getInstance(v.getContext()).logContactPhoneBook();
            }
        } else {
            requestPermissions();
        }
    }

    @OnClick(R.id.button_warn)
    void onClickWarn(View v) {
        v.setEnabled(false);
        v.postDelayed(() -> v.setEnabled(true), 500);
        if (checkPermissions(v.getContext())) {
            v.setEnabled(true);
            v.setVisibility(View.GONE);
        } else {
            requestPermissions();
        }
    }

    @OnClick(R.id.button_back)
    void onClick() {
        getActivity().onBackPressed();
    }
}
