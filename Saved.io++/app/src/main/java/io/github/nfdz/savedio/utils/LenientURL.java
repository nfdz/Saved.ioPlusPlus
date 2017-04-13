/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.utils;

import android.text.TextUtils;

public class LenientURL {

    private static final String PROTOCOL_SEPARATOR = "://";

    public static String processURL(String rawUrl) {

        if (TextUtils.isEmpty(rawUrl)) {
            return "";
        }

        if (rawUrl.contains(PROTOCOL_SEPARATOR)) {
            return rawUrl;
        } else {
            return "http://" + rawUrl;
        }
    }
}
