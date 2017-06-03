/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.model.serialization;

import android.support.annotation.StringRes;

public class SerializationException extends Exception {

    static final long serialVersionUID = 1L;

    private @StringRes int mMsgId;

    public SerializationException(@StringRes int msgId) {
        super();
        mMsgId = msgId;
    }

    public @StringRes int getMessageId() {
        return mMsgId;
    }
}
