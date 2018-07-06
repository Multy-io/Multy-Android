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
import android.support.annotation.Nullable;
import android.util.Log;

import io.multy.util.Constants;

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

        MultySyncAdapter(Context context, boolean autoInitialize) {
            super(context, autoInitialize);
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority,
                                  ContentProviderClient provider, SyncResult syncResult) {
            logSync(account, extras, authority, provider, syncResult);
//            Cursor dirtyContacts = getDirtyContacts(provider);
//            if (dirtyContacts == null) {
//                return;
//            }
//            dirtyContacts.moveToFirst();
//            ContactUtils.logCursor(dirtyContacts);
//            Realm realm = Realm.getInstance(Multy.getRealmConfiguration());
//            ArrayList<Long> listToDelete = new ArrayList<>();
////            ArrayList<Long> listToEdit = new ArrayList<>();
//            do {
//                String deleted = dirtyContacts.getString(dirtyContacts.getColumnIndex(ContactsContract.RawContacts.DELETED));
//                if (deleted != null && deleted.equals("1")) {
//                    listToDelete.add(dirtyContacts.getLong(dirtyContacts.getColumnIndex(ContactsContract.RawContacts._ID)));
//                } else {
////                    listToEdit.add(dirtyContacts.getLong(dirtyContacts.getColumnIndex(ContactsContract.RawContacts._ID)));
//                }
//                if (listToDelete.size() > 0) {
//                    ContactUtils.deleteMultyContacts(getContext(), listToDelete);
//                    realm.executeTransaction(realm1 -> {
//                        RealmResults<Contact> contacts = realm.where(Contact.class)
//                                .in(Contact.ID, listToDelete.toArray(new Long[listToDelete.size()])).findAll();
//                        contacts.deleteAllFromRealm();
//                    });
////                }
////                if (listToEdit.size() > 0) {
////                    RealmResults<Contact> contacts = realm.where(Contact.class)
////                            .in(Contact.ID, listToEdit.toArray(new Long[listToDelete.size()])).findAll();
////                    ArrayList<Long> parentIdsList = new ArrayList<>();
////                    for (Contact contact : contacts) {
////                        parentIdsList.add(contact.getParentId());
////                    }
////                    provider.
//                }
//            } while (dirtyContacts.moveToNext());
//            realm.close();
//            dirtyContacts.close();
        }

        private Cursor getDirtyContacts(ContentProviderClient provider) {
            String selection = ContactsContract.RawContacts.DIRTY + " = ? AND " +
                    ContactsContract.RawContacts.ACCOUNT_TYPE + " = ? AND " +
                    ContactsContract.RawContacts.ACCOUNT_NAME + " = ?";
            try {
                String[] args = new String[] {
                        "1",
                        Constants.ACCOUNT_TYPE,
                        Constants.ACCOUNT_NAME
                };
                return provider.query(ContactsContract.RawContacts.CONTENT_URI, null, selection, args, null);
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
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
                    Log.i(getClass().getSimpleName(), "syncresult: fullSyncRequested" + syncResult.fullSyncRequested);
                    Log.i(getClass().getSimpleName(), "syncresult: syncAlreadyInProgress" + syncResult.syncAlreadyInProgress);
                    Log.i(getClass().getSimpleName(), "syncresult: toDebugString" + syncResult.toDebugString());
                } else {
                    Log.i(getClass().getSimpleName(), "syncresult = null");
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
