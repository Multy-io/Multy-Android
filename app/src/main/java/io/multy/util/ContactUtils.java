/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.multy.R;
import io.multy.model.entities.Contact;
import io.multy.storage.RealmManager;
import io.realm.Realm;

/**
 * Created by anschutz1927@gmail.com on 21.06.18.
 */
public class ContactUtils {

    public final static String EXTRA_ACTION = "EXTRA_CONTACT_ACTION";
    public final static int EXTRA_ACTION_OPEN_CONTACTS = 1100;
    public final static int EXTRA_ACTION_OPEN_CONTACT = 1101;
    public final static int EXTRA_ACTION_OPEN_SEND = 1102;
    public final static String EXTRA_RAW_CONTACT_ID = "EXTRA_SHOW_SEND";

    private static Uri buildSyncAdapterUri(Uri uri) {
        return uri.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

    public static void createLocalContact(long multyRowId, long parentRowId, Cursor contactCursor, Realm.Transaction.OnSuccess onSuccess) {
        Collection<Contact> contacts = new ArrayList<>();
        do {
            Contact contact = new Contact(
                    multyRowId,
                    parentRowId,
                    contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)),
                    contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
            );
            contacts.add(contact);
        } while(contactCursor.moveToNext());
        RealmManager.getSettingsDao().saveContacts(contacts, onSuccess);
        contactCursor.moveToFirst();
    }

    public static void addAddressToLocalContact(long multyRowId, String address, int currencyId, int networkId,
                                                int currencyImgId, Realm.Transaction.OnSuccess onSuccess) {
        RealmManager.getSettingsDao().saveAddressesToContact(multyRowId, address, currencyId, networkId, currencyImgId, onSuccess);
    }

    public static void logCursor(Cursor cursor) {
        Log.i(ContactUtils.class.getSimpleName(), "Cursor size = " + String.valueOf(cursor.getCount()));
        if (cursor.getCount() > 0) {
            do {
                for (String column : cursor.getColumnNames()) {
                    int index = cursor.getColumnIndex(column);
                    String result;
                    switch (cursor.getType(index)) {
                        case Cursor.FIELD_TYPE_INTEGER:
                            result = String.valueOf(cursor.getInt(index));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            result = String.valueOf(cursor.getFloat(index));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            result = cursor.getString(index);
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            result = "blob";
                            break;
                        default:
                            result = "null";
                    }
                    result = column + " " + result;
                    Log.i(ContactUtils.class.getSimpleName(), result);
                }
            } while (cursor.moveToNext());
        }
        cursor.moveToFirst();
    }

    public static boolean isMultyLinkCreated(Context context, Cursor selectedContact) {
        if (!selectedContact.moveToFirst()) {
            return false;
        }
        final String contactId = selectedContact.getString(selectedContact.getColumnIndex(ContactsContract.Contacts._ID));
        String[] projection = new String[] {
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.RawContacts.ACCOUNT_NAME,
                ContactsContract.RawContacts.ACCOUNT_TYPE
        };
        final String selection = ContactsContract.RawContacts.CONTACT_ID + " = ? AND " +
                ContactsContract.RawContacts.ACCOUNT_TYPE + " = ?";
        final String[] args = new String[] {contactId, Constants.ACCOUNT_TYPE};
        Cursor selectedRaw = context.getContentResolver()
                .query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, args, null);
        if (selectedRaw != null && selectedRaw.getCount() > 0 && selectedRaw.moveToFirst()) {
            logCursor(selectedRaw);
            selectedRaw.close();
            return true;
        }
        return false;
    }

    public static boolean isMultyAddressLinked(Context context, String formattedAddress) {
        String selection = ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.Data.DATA3 + " = ?";
        String[] args = new String[] { Constants.CONTACT_MIMETYPE, formattedAddress };
        Cursor rawContactData = context.getContentResolver()
                .query(ContactsContract.Data.CONTENT_URI, null, selection, args, null);
        if (rawContactData != null) {
            if (rawContactData.moveToFirst()) {
                logCursor(rawContactData);
            }
            if (rawContactData.getCount() > 0) {
                return true;
            }
            rawContactData.close();
        }
        return false;
    }

    public static long addContact(Context context, Cursor contactCursor, String parentRowId, @Nullable String formattedAddress,
                                  @Nullable Integer currencyId, @Nullable Integer networkId, @Nullable String address) {
        String displayName = contactCursor
                .getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        String name = null;
        String family = null;
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                ContactsContract.Data.MIMETYPE
        };
        String selection = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                ContactsContract.Data.DISPLAY_NAME_PRIMARY + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ? ";
        String[] args = new String[] {
                contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID)),
                contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)),
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
        };
        Cursor contactData = context.getContentResolver()
                .query(ContactsContract.Data.CONTENT_URI, projection, selection, args, null);
        if (contactData != null && contactData.moveToFirst()) {
            do {
                name = contactData.getString(0);
                family = contactData.getString(1);
            } while (contactData.moveToNext() && name == null && family == null);
            contactData.close();
        }
        return ContactUtils.addContact(context, displayName, name, family, parentRowId, formattedAddress, currencyId, networkId, address);
    }

    private static long addContact(Context context, String displayName, String name, String family, String parentRowId,
                                   @Nullable String formattedAddress, @Nullable Integer currencyId,
                                   @Nullable Integer networkId, @Nullable String address) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(buildSyncAdapterUri(ContactsContract.RawContacts.CONTENT_URI))
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, Constants.ACCOUNT_NAME)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
                .withValue(ContactsContract.RawContacts.SYNC1, parentRowId)
                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
                .build());
        operations.add(ContentProviderOperation.newInsert(buildSyncAdapterUri(ContactsContract.Data.CONTENT_URI))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, family)
                .build());
        operations.add(ContentProviderOperation.newInsert(buildSyncAdapterUri(ContactsContract.Data.CONTENT_URI))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, Constants.CONTACT_MIMETYPE)
                .withValue(ContactsContract.Data.DATA2, null)
                .withValue(ContactsContract.Data.DATA3, context.getString(R.string.contact_default_link_name))
                .build());
        if (formattedAddress != null) {
            operations.add(ContentProviderOperation.newInsert(buildSyncAdapterUri(ContactsContract.Data.CONTENT_URI))
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, Constants.CONTACT_MIMETYPE)
                    .withValue(ContactsContract.Data.DATA2, generateData(currencyId, networkId, address))
                    .withValue(ContactsContract.Data.DATA3, formattedAddress)
                    .build());
        }
        try {
            ContentProviderResult[] results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
            if (results.length > 0 && !TextUtils.isEmpty(results[0].uri.getLastPathSegment())) {
                return Long.parseLong(results[0].uri.getLastPathSegment());
            }
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static String generateData(Integer currencyId, Integer networkId, String address) {
        return currencyId + "\n" + networkId + "\n" + address;
    }

    public static void updateContact(Context context, String rawMultyContactId, @NonNull String formattedAddress,
                                     Integer currencyId, Integer networkId, String address) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(buildSyncAdapterUri(ContactsContract.Data.CONTENT_URI))
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawMultyContactId)
                .withValue(ContactsContract.Data.MIMETYPE, Constants.CONTACT_MIMETYPE)
                .withValue(ContactsContract.Data.DATA2, generateData(currencyId, networkId, address))
                .withValue(ContactsContract.Data.DATA3, formattedAddress)
                .build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public static void updateContactName(Context context, String rawMultyContactId, String name, String family, String displayName) {
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//        String selection = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
//                ;
//        String[] args;
//        operations.add(ContentProviderOperation.newUpdate(buildSyncAdapterUri(ContactsContract.Data.CONTENT_URI))
//                .withSelection(selection, args)
//                .withValue()
//                .build());
//        try {
//            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
//        } catch (RemoteException | OperationApplicationException e) {
//            e.printStackTrace();
//        }
    }

    public static void deleteMultyAddress(Context context, String address, int currencyId,
                                          int networkId, Realm.Transaction.OnSuccess onSuccess) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        String[] projection = new String[] {ContactsContract.Data._ID};
        String selection = ContactsContract.Data.DATA2 + " = ? ";
        String[] args = new String[] { generateData(currencyId, networkId, address) };
        Cursor dataCursor = context.getContentResolver()
                .query(ContactsContract.Data.CONTENT_URI, projection, selection, args, null);
        if (dataCursor != null) {
            dataCursor.moveToFirst();
            do {
                String dataId = dataCursor.getString(0);
                operations.add(ContentProviderOperation.newDelete(buildSyncAdapterUri(ContactsContract.Data.CONTENT_URI))
                        .withSelection(ContactsContract.Data._ID + " = ? ", new String[]{ dataId })
                        .build());
            } while (dataCursor.moveToNext());
            dataCursor.close();
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
        RealmManager.getSettingsDao().removeAddressFromContact(address, onSuccess);
    }

    public static void deleteMultyContact(Context context, long contactId, Realm.Transaction.OnSuccess onSuccess) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newDelete(buildSyncAdapterUri(ContactsContract.RawContacts.CONTENT_URI))
                .withSelection(ContactsContract.RawContacts._ID + " = ? ", new String[] { String.valueOf(contactId) })
                .build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
        RealmManager.getSettingsDao().removeContact(contactId, onSuccess);
    }

    public static void deleteMultyContacts(Context context, List<Long> idsToRemove) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (long id : idsToRemove) {
            operations.add(ContentProviderOperation.newDelete(buildSyncAdapterUri(ContactsContract.RawContacts.CONTENT_URI))
                    .withSelection(ContactsContract.RawContacts._ID + " = ? ", new String[] { String.valueOf(id) })
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllMultyContacts(Context context, Realm.Transaction.OnSuccess onSuccess) {
        String[] projection = new String[] {ContactsContract.RawContacts._ID};
        String selection = ContactsContract.RawContacts.ACCOUNT_NAME + " = ? AND " +
                ContactsContract.RawContacts.ACCOUNT_TYPE + " = ?";
        String[] args = new String[] { Constants.ACCOUNT_NAME, Constants.ACCOUNT_TYPE };
        Cursor deleteItems = context.getContentResolver()
                .query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, args, null);
        if (deleteItems != null && deleteItems.moveToFirst()) {
            ArrayList<ContentProviderOperation> deleteOperations = new ArrayList<>();
            do {
                String id = deleteItems.getString(deleteItems.getColumnIndex(ContactsContract.RawContacts._ID));
                deleteOperations.add(ContentProviderOperation.newDelete(buildSyncAdapterUri(ContactsContract.RawContacts.CONTENT_URI))
                        .withSelection(ContactsContract.RawContacts._ID + " = ?", new String[] { id })
                        .build());
            } while (deleteItems.moveToNext());
            deleteItems.close();
            try {
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, deleteOperations);
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
        }
        RealmManager.getSettingsDao().removeAllContacts(onSuccess);
    }

    public static @Nullable String getMultyContactRawId(Context context, Cursor selectedContact) {
        if (!selectedContact.moveToFirst()) {
            return null;
        }
        final String contactId = selectedContact.getString(selectedContact.getColumnIndex(ContactsContract.Contacts._ID));
        String[] projection = new String[] {
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.RawContacts.ACCOUNT_NAME,
                ContactsContract.RawContacts.ACCOUNT_TYPE
        };
        final String selection = ContactsContract.RawContacts.CONTACT_ID + " = ? AND " +
                ContactsContract.RawContacts.ACCOUNT_TYPE + " = ?";
        final String[] args = new String[] {contactId, Constants.ACCOUNT_TYPE};
        Cursor selectedRaw = context.getContentResolver()
                .query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, args, null);
        if (selectedRaw != null && selectedRaw.getCount() > 0 && selectedRaw.moveToFirst()) {
            logCursor(selectedRaw);
            final String rawId = selectedRaw.getString(selectedRaw.getColumnIndex(ContactsContract.RawContacts._ID));
            selectedRaw.close();
            return rawId;
        }
        return null;
    }
}
