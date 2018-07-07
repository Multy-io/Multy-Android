/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

import android.support.annotation.NonNull;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by anschutz1927@gmail.com on 21.06.18.
 */
public class Contact extends RealmObject {

    public static final String ID = "id";

    @PrimaryKey
    private long id;
    private long parentId;
    private String name;
    private String photoUri;
    private RealmList<ContactAddress> addresses;

    public Contact() { }

    public Contact(long id, long parentId, String name, String photoUri) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.photoUri = photoUri;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    @NonNull
    public RealmList<ContactAddress> getAddresses() {
        return addresses == null ? new RealmList<>() : addresses;
    }

    public void setAddresses(RealmList<ContactAddress> addresses) {
        this.addresses = addresses;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }
}
