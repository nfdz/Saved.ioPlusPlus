/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.utils;

import android.text.TextUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtils {

    private static final String PROTOCOL_SEPARATOR = "://";

    /**
     * Processes given URL to ensure that it has a valid protocol. If it has not, it will add
     * http protocol by default.
     * @param rawUrl
     * @return URL
     */
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

    /**
     * Tries to infer the favicon path with given URL.
     * @param rawUrl path.
     * @return inferred favicon path or null.
     */
    public static String getFaviconPath(String rawUrl) {
        String faviconPath = null;
        try {
            URL url = new URL(rawUrl);
            String basePath = url.getProtocol() + "://" + url.getHost();
            faviconPath = basePath + "/favicon.ico";
        } catch (MalformedURLException e) {
            // swallow
        }
        return faviconPath;
    }
}
