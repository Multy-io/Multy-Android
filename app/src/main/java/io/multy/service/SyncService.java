/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.service;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import io.multy.Multy;
import io.multy.model.entities.Contact;
import io.multy.storage.SettingsDao;
import io.multy.util.Constants;
import io.multy.util.ContactUtils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by anschutz1927@gmail.com on 21.06.18.
 */
public class SyncService extends Service {

    private static final Object syncObject = new Object();
    private static MultySyncAdapter syncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (syncObject) {
//            Debug.waitForDebugger();
            if (syncAdapter == null) {
                syncAdapter = new MultySyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }

    public static class MultySyncAdapter extends AbstractThreadedSyncAdapter {

        ArrayList<Long> listToDelete = new ArrayList<>();

        MultySyncAdapter(Context context, boolean autoInitialize) {
            super(context, autoInitialize);
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority,
                                  ContentProviderClient provider, SyncResult syncResult) {
            Realm realm = Realm.getInstance(Multy.getRealmConfiguration(getContext()));
            logSync(account, extras, authority, provider, syncResult);
            //check for deleted raw contacts
            Cursor deletedContacts = getMultyDeletedContacts(provider);
            if (deletedContacts != null) {
                if (deletedContacts.moveToFirst()) {
                    ContactUtils.logCursor(deletedContacts);
                    do {
                        listToDelete.add(deletedContacts.getLong(deletedContacts.getColumnIndex(ContactsContract.RawContacts._ID)));
                    } while (deletedContacts.moveToNext());
                } else {
                    Log.i(getClass().getSimpleName(), "No deleted 'Multy' contacts.");
                }
                deletedContacts.close();
            } else {
                Log.i(getClass().getSimpleName(), "Deleted 'Multy' contacts is null.");
            }
            //sync existing contacts data
            Cursor contacts = getMultyContacts(provider, realm);
            if (contacts != null) {
                if (contacts.moveToFirst()) {
                    ContactUtils.logCursor(contacts);
                    do {
                        if (sync(contacts, provider, realm, syncResult)) {
                            syncResult.stats.numUpdates++;
                        }
                    } while (contacts.moveToNext());
                } else {
                    Log.i(getClass().getSimpleName(), "No 'Multy' contacts.");
                }
                contacts.close();
            } else {
                Log.i(getClass().getSimpleName(), "'Multy' contacts is null.");
            }
            if (listToDelete.size() > 0) {
                ContactUtils.deleteMultyContacts(getContext(), listToDelete);
                realm.executeTransaction(realm1 -> {
                    RealmResults<Contact> contactsRealm = realm.where(Contact.class)
                            .in(Contact.ID, listToDelete.toArray(new Long[listToDelete.size()])).findAll();
                    contactsRealm.deleteAllFromRealm();
                });
                syncResult.stats.numDeletes += listToDelete.size();
                listToDelete.clear();
            }
            realm.close();
        }

        /**
         * @param provider ContentProviderClient
         * @return selection from RawContact
         */
        private Cursor getMultyDeletedContacts(ContentProviderClient provider) {
            String selection = ContactsContract.RawContacts.DELETED + " = ? AND " +
                    ContactsContract.RawContacts.ACCOUNT_TYPE + " = ?";
            String[] args = new String[]{"1", Constants.ACCOUNT_TYPE};
            try {
                return provider.query(ContactsContract.RawContacts.CONTENT_URI, null, selection, args, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * @param provider ContentProviderClient
         * @param realm Realm instance
         * @return selection from Data
         */
        private Cursor getMultyContacts(ContentProviderClient provider, Realm realm) {
            RealmResults<Contact> contacts = realm.where(Contact.class).findAll();
            if (contacts.size() == 0) {
                return null;
            }
            try {
                StringBuilder querrySelection = new StringBuilder(ContactsContract.Data.RAW_CONTACT_ID + " IN (");
                String[] querryArgs = new String[contacts.size() + 1];
                for (int i = 0; i < contacts.size(); i++) {
                    querrySelection.append("?");
                    if (i != contacts.size() - 1) {
                        querrySelection.append(",");
                    }
                    querryArgs[i] = String.valueOf(contacts.get(i).getId());
                }
                querrySelection.append(") AND " + ContactsContract.Data.MIMETYPE + "=?");
                querryArgs[querryArgs.length - 1] = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
                return provider.query(ContactsContract.Data.CONTENT_URI,
                        null, querrySelection.toString(), querryArgs, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private void logSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
            try {
                Log.i(getClass().getSimpleName(), "Sync adapter called.");
                Log.i(getClass().getSimpleName(), "extras == null ? - " + (extras == null));
                if (extras != null) {
                    Log.i(getClass().getSimpleName(), "extras.hasextra ? - " + extras.toString());
                }
                if (account != null) {
                    Log.i(getClass().getSimpleName(), "account - " + account.name);
                    Log.i(getClass().getSimpleName(), "account - " + account.type);
                } else {
                    Log.i(getClass().getSimpleName(), "account = null");
                }
                if (authority != null) {
                    Log.i(getClass().getSimpleName(), "authority - " + authority);
                } else {
                    Log.i(getClass().getSimpleName(), "authority = null");
                }
                if (provider != null) {
                    Log.i(getClass().getSimpleName(), "provider: " + provider.toString());
                } else {
                    Log.i(getClass().getSimpleName(), "provider = null");
                }
                if (syncResult != null) {
                    Log.i(getClass().getSimpleName(), "syncresult: fullSyncRequested " + syncResult.fullSyncRequested);
                    Log.i(getClass().getSimpleName(), "syncresult: syncAlreadyInProgress " + syncResult.syncAlreadyInProgress);
                    Log.i(getClass().getSimpleName(), "syncresult: toDebugString " + syncResult.toDebugString());
                } else {
                    Log.i(getClass().getSimpleName(), "syncresult = null");
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        private boolean sync(Cursor contacts, ContentProviderClient provider, Realm realm, SyncResult syncResult) {
            boolean result = false;
            final String multyDisplayName = contacts.getString(contacts
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
            final String multyGivenName = contacts.getString(contacts
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            final String multyFamilyName = contacts.getString(contacts
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            final String nameRawId = contacts.getString(contacts.getColumnIndex(ContactsContract.Data.NAME_RAW_CONTACT_ID));
            final String multyRawId = contacts.getString(contacts.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
            if (syncNames(multyDisplayName, multyGivenName, multyFamilyName,
                    nameRawId, multyRawId, provider, realm, syncResult)) {
                result = true;
            }
            final String contactId = contacts.getString(contacts.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            if (syncPhotos(contactId, multyRawId, provider, realm, syncResult)) {
                result = true;
            }
            return result;
        }

        private boolean syncNames(String multyDiplayName, String multyGivenName, String multyFamilyName, String nameRawId,
                             String multyRawId, ContentProviderClient provider, Realm realm, SyncResult syncResult) {
            String selection = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ? ";
            String[] args = new String[] {nameRawId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
            try {
                Cursor nameData = provider.query(ContactsContract.Data.CONTENT_URI, null, selection, args, null);
                if (nameData == null) {
                    return false;
                } else if (!nameData.moveToFirst()) {
                    nameData.close();
                    return false;
                }
                final String contactDisplayName = nameData.getString(nameData
                        .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
                final String contactGivenName = nameData.getString(nameData
                        .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                final String contactFamilyName = nameData.getString(nameData
                        .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                if (isStringsEquals(multyDiplayName, contactDisplayName) &&
                        isStringsEquals(multyGivenName, contactGivenName) &&
                        isStringsEquals(multyFamilyName, contactFamilyName)) {
                    nameData.close();
                    return false;
                }
                if (ContactUtils.renameMultyContact(getContext(), multyRawId, contactGivenName, contactFamilyName, contactDisplayName)) {
                    Contact contact = realm.where(Contact.class).equalTo(Contact.ID, Long.valueOf(multyRawId)).findFirst();
                    if (contact == null) {
                        listToDelete.add(Long.valueOf(multyRawId));
                    } else {
                        renameLocalContact(contact, contactDisplayName);
                    }
                }
                nameData.close();
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
                syncResult.stats.numIoExceptions++;
            }
            return false;
        }

        private boolean syncPhotos(String contactId, String multyRawId,
                                   ContentProviderClient provider, Realm realm, SyncResult syncResult) {
            try {
                final String multyPhotoUri = getMultyPhotoUri(realm, multyRawId);
                final String contactPhotoUri = getContactPhotoUri(provider, contactId);
                if (!isStringsEquals(multyPhotoUri, contactPhotoUri)) {
                    Contact contact = realm.where(Contact.class).equalTo(Contact.ID, Long.valueOf(multyRawId)).findFirst();
                    if (contact == null) {
                        listToDelete.add(Long.valueOf(multyRawId));
                    } else {
                        updateLocalContactPhoto(contact, contactPhotoUri);
                        return true;
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                syncResult.stats.numIoExceptions++;
            }
            return false;
        }

        private boolean isStringsEquals(String string1, String string2) {
            return TextUtils.isEmpty(string1) && TextUtils.isEmpty(string2) ||
                    (!TextUtils.isEmpty(string1) || TextUtils.isEmpty(string2)) &&
                            (TextUtils.isEmpty(string1) || !TextUtils.isEmpty(string2)) && string1.equals(string2);
        }

        /**
         * Get array of uris where index 0 is photo uri and index 1 is photo thumbnail uri
         * @param provider ContantProviderClient
         * @param contactId id of multy contact
         * @return array of uris (or nulls)
         */
        @NonNull
        private String[] getContactPhotoUris(ContentProviderClient provider, String contactId) throws RemoteException {
            String[] photoUris = new String[2];
            String selection = ContactsContract.Contacts._ID + " = " + contactId;
            Cursor contact = provider.query(ContactsContract.Contacts.CONTENT_URI,
                    null, selection, null, null);
            if (contact != null) {
                if (contact.moveToFirst()) {
                    photoUris[0] = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                    photoUris[1] = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                }
                contact.close();
            }
            return photoUris;
        }

        /**
         * Get array of uris where index 0 is photo uri and index 1 is photo thumbnail uri
         * @param provider ContentProviderClient
         * @param multyRawId id of multy from RawContact
         * @return array of uris (or nulls)
         */
        @NonNull
        private String[] getMultyPhotoUris(ContentProviderClient provider, String multyRawId) throws RemoteException {
            String[] photoUris = new String[2];
            String selection = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
                    ContactsContract.Data.MIMETYPE + " = ? ";
            String[] args = new String[] {multyRawId, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE};
            Cursor contactData = provider.query(ContactsContract.Data.CONTENT_URI, null, selection, args, null);
            if (contactData != null) {
                if (contactData.moveToFirst()) {
                    photoUris[0] = contactData.getString(contactData
                            .getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI));
                    photoUris[1] = contactData.getString(contactData
                            .getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI));
                }
                contactData.close();
            }
            return photoUris;
        }

        private String getMultyPhotoUri(Realm realm, String multyRawId) {
            SettingsDao dao = new SettingsDao(realm);
            Contact contact = dao.getContact(Long.parseLong(multyRawId));
            if (contact != null) {
                return contact.getPhotoUri();
            }
            return null;
        }

        private String getContactPhotoUri(ContentProviderClient provider, String contactId) throws RemoteException {
            String photoUri = null;
            String selection = ContactsContract.Contacts._ID + " = " + contactId;
            Cursor contact = provider.query(ContactsContract.Contacts.CONTENT_URI,
                    null, selection, null, null);
            if (contact != null) {
                if (contact.moveToFirst()) {
                    photoUri = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                }
                contact.close();
            }
            return photoUri;
        }

        private void renameLocalContact(Contact contact, String name) {
            SettingsDao dao = new SettingsDao(contact.getRealm());
            dao.renameContact(contact, name);
        }

        private void updateLocalContactPhoto(Contact contact, String photoUri) {
            SettingsDao dao = new SettingsDao(contact.getRealm());
            dao.updateContactPhoto(contact, photoUri);
        }

//        private boolean isContactDeleted(Cursor contact, long contactId) {
//            String deleted = contact.getString(contact.getColumnIndex(ContactsContract.RawContacts.DELETED));
//            if (deleted != null && deleted.equals("1")) {
//                listToDelete.add(contactId);
//                return true;
//            }
//            return false;
//        }

//        private boolean isContactRenamed(Realm realm, Cursor dirtyContacts, long contactId) {
//            final String rawContactName = dirtyContacts.getString(dirtyContacts
//                    .getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
//            Contact contact = realm.where(Contact.class).equalTo(Contact.ID, contactId).findFirst();
//            if (contact == null) {
//                listToDelete.add(contactId);
//                return true;
//            }
//            if (!contact.getName().equals(rawContactName)) {
//                renameLocalContact(contact, rawContactName);
//                return true;
//            }
//            return false;
//        }

//        private boolean isContactEdited(Realm realm, Cursor dirtyContacts, ContentProviderClient provider, long contactId)
//                throws RemoteException {
//            final String multyDisplayName = dirtyContacts.getString(dirtyContacts
//                    .getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
//            String selectionMulty = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
//                    ContactsContract.Data.MIMETYPE + " = ? ";
//            String[] argsMulty = new String[]{String.valueOf(contactId),
//                  ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
//            Cursor multyNameData = provider
//                    .query(ContactsContract.Data.CONTENT_URI, null, selectionMulty, argsMulty, null);
//            if (multyNameData == null) {
//                return false;
//            }
//            multyNameData.moveToFirst();
//            ContactUtils.logCursor(multyNameData);
//            final String nameContactId = multyNameData.getString(multyNameData
//                  .getColumnIndex(ContactsContract.Data.NAME_RAW_CONTACT_ID));
//            multyNameData.close();
//            if (nameContactId == null) {
//                return false;
//            }
//            String selectionContact = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " +
//                    ContactsContract.Data.MIMETYPE + " = ? ";
//            String[] argsContact = new String[]{nameContactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
//            Cursor contactNameData = provider
//                    .query(ContactsContract.Data.CONTENT_URI, null, selectionContact, argsContact, null);
//            if (contactNameData == null) {
//                return false;
//            }
//            contactNameData.moveToFirst();
//            ContactUtils.logCursor(contactNameData);
//            final String contactDisplayName = contactNameData.getString(contactNameData
//                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
//            if (contactDisplayName.equals(multyDisplayName)){
//                contactNameData.close();
//                return true;
//            }
//            final String name = contactNameData.getString(contactNameData
//                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
//            final String family = contactNameData.getString(contactNameData
//                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
//            contactNameData.close();
//            if (ContactUtils.renameMultyContact(getContext(), String.valueOf(contactId), name, family, contactDisplayName)) {
//                Contact contact = realm.where(Contact.class).equalTo(Contact.ID, contactId).findFirst();
//                if (contact == null) {
//                    listToDelete.add(contactId);
//                } else {
//                    renameLocalContact(contact, contactDisplayName);
//                    return true;
//                }
//            }
//            return false;
//        }
    }
}
