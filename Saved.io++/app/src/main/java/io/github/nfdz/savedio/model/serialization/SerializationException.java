/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.model.serialization;

import android.support.annotation.StringRes;

/**
 * Custom exception class to manage serialization exception.
 */
public class SerializationException extends Exception {

    static final long serialVersionUID = 1L;

    private @StringRes int mMsgId;

    /**
     * Exception message has to be a string resource in order to be accessible to any user.
     * @param msgId
     */
    public SerializationException(@StringRes int msgId) {
        super();
        mMsgId = msgId;
    }

    public @StringRes int getMessageId() {
        return mMsgId;
    }
}
