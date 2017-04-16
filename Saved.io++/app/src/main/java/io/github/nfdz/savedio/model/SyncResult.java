/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.model;


import io.realm.RealmObject;

public class SyncResult extends RealmObject {

    public static final String FIELD_SUCCESS = "mSuccess";
    public static final String FIELD_MESSAGE = "mMessage";

    private boolean mSuccess;

    private String mMessage;

    public boolean isSuccess() {
        return mSuccess;
    }

    public void setSuccess(boolean success) {
        mSuccess = success;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }
}
