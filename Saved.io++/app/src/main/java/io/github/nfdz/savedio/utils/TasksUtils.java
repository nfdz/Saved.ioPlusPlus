/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.utils;


import java.util.Random;

import io.github.nfdz.savedio.Callback;
import io.github.nfdz.savedio.data.RealmUtils;
import io.github.nfdz.savedio.model.Bookmark;
import io.realm.Realm;

/**
 * This class contains static methods to ease work with common application tasks.
 */
public class TasksUtils {

    private static final String TAG = TasksUtils.class.getSimpleName();

    /**
     * This method creates a new bookmark. It manages all related thing like send to server or
     * store in persistence.
     * @param realm it has to be initialized.
     * @param bookmark unmanaged object with bookmark information (it should not have ID or date
     *                 because it is the server that assigns it).
     * @param callback to be notified.
     */
    public static void createBookmark(final Realm realm,
                                      final Bookmark bookmark,
                                      final Callback<Void> callback) {
        // TODO create bookmark in server
        bookmark.setId("" + new Random().nextInt(Integer.MAX_VALUE));
        bookmark.setDate("AAAA:BB:SS ASD AS 0");

        // store it in persistence
        RealmUtils.addBookmark(realm, bookmark, callback);
    }

    /**
     * This method removes a bookmark with given ID. It manages all related thing like remove in
     * server or persistence.
     * @param realm it has to be initialized.
     * @param bookmarkId ID of the bookmark that will be removed.
     * @param callback to be notified.
     */
    public static void deleteBookmark(Realm realm, String bookmarkId, Callback<Bookmark> callback) {
        // TODO remove bookmark in server

        RealmUtils.removeBookmark(realm, bookmarkId, callback);
    }
}
