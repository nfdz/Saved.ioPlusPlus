/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.utils;


import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.github.nfdz.savedio.BuildConfig;
import io.github.nfdz.savedio.Callbacks;
import io.github.nfdz.savedio.data.PreferencesUtils;
import io.github.nfdz.savedio.data.RealmUtils;
import io.github.nfdz.savedio.model.Bookmark;
import io.github.nfdz.savedio.sync.api.APIHelper;
import io.github.nfdz.savedio.sync.api.BookmarkAPI;
import io.github.nfdz.savedio.sync.api.CreateBookmarkResponse;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;

/**
 * This class contains static methods to ease work with common application tasks.
 */
public class TasksUtils {

    /**
     * This method creates a new bookmark. It manages all related thing like send to server or
     * store in persistence.
     * @param context
     * @param realm it has to be initialized.
     * @param bookmark unmanaged object with bookmark information (it should not have ID or date
     *                 because it is the server that assigns it).
     * @param callback to be notified.
     */
    public static void createBookmark(final Context context,
                                      final Realm realm,
                                      final Bookmark bookmark,
                                      final Callbacks.OperationCallback<Void> callback) {
        if (!TextUtils.isEmpty(PreferencesUtils.getUserAPIKey(context))) {
            // create bookmark in server
            new AsyncTask<Void, Void, Void>() {
                private String bookmarkId;
                private String bookmarkDate;
                private String errorMsg;
                private Throwable errorTh;

                @Override
                protected Void doInBackground(Void... params) {
                    APIHelper helper = new APIHelper();
                    String devKey = BuildConfig.SAVEDIO_API_DEV_KEY;
                    String userKey = PreferencesUtils.getUserAPIKey(context);
                    String list = bookmark.getListName();
                    Call<CreateBookmarkResponse> createCall = helper.getAPI().createBookmark(devKey,
                            userKey,
                            bookmark.getUrl(),
                            bookmark.getTitle(),
                            TextUtils.isEmpty(list) ? null : list);

                    try {
                        Response<CreateBookmarkResponse> createRes = createCall.execute();
                        if (createRes.isSuccessful()) {
                            String bmId = createRes.body().id;
                            Call<BookmarkAPI> bmCall = helper.getAPI().retrieveSingleBookmark(
                                    bmId,
                                    devKey,
                                    userKey);
                            Response<BookmarkAPI> bmRes = bmCall.execute();
                            if (bmRes.isSuccessful()) {
                                bookmarkId = bmId;
                                bookmarkDate = bmRes.body().date;
                            } else {
                                errorMsg = bmRes.raw().message();
                            }
                        } else {
                            errorMsg = createRes.raw().message();
                        }
                    } catch (IOException e) {
                        errorMsg = e.getMessage();
                        errorTh = e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void v) {
                    if (!TextUtils.isEmpty(bookmarkId) &&
                            !TextUtils.isEmpty(bookmarkDate)) {
                        // set id and date
                        bookmark.setId(bookmarkId);
                        bookmark.setDate(bookmarkDate);
                        // store it in persistence
                        RealmUtils.addBookmark(realm, bookmark, callback);
                    } else {
                        callback.onError(errorMsg, errorTh);
                    }
                }
            }.execute();
        } else {
            // set id and date
            SimpleDateFormat sdf = new SimpleDateFormat(Bookmark.DATE_FORMAT);
            String currentDate = sdf.format(new Date());
            bookmark.setId(UUID.randomUUID().toString());
            bookmark.setDate(currentDate);
            // store it in persistence
            RealmUtils.addBookmark(realm, bookmark, callback);
        }
    }

    /**
     * This method removes a bookmark with given ID. It manages all related thing like remove in
     * server or persistence.
     * @param context
     * @param realm it has to be initialized.
     * @param bookmarkId ID of the bookmark that will be removed.
     * @param callback to be notified.
     */
    public static void deleteBookmark(final Context context,
                                      final Realm realm,
                                      final String bookmarkId,
                                      final Callbacks.OperationCallback<Bookmark> callback) {
        if (!TextUtils.isEmpty(PreferencesUtils.getUserAPIKey(context))) {
            // remove bookmark in server
            new AsyncTask<Void, Void, Void>() {
                private boolean success;
                private String errorMsg;
                private Throwable errorTh;

                @Override
                protected Void doInBackground(Void... params) {
                    APIHelper helper = new APIHelper();
                    String devKey = BuildConfig.SAVEDIO_API_DEV_KEY;
                    String userKey = PreferencesUtils.getUserAPIKey(context);
                    Call<Void> call = helper.getAPI().deleteBookmark(devKey,
                            userKey,
                            bookmarkId);

                    try {
                        Response<Void> res = call.execute();
                        success = res.isSuccessful();
                        if (!success) {
                            errorMsg = res.raw().message();
                        }
                    } catch (IOException e) {
                        errorMsg = e.getMessage();
                        errorTh = e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void v) {
                    if (success) {
                        RealmUtils.removeBookmark(realm, bookmarkId, callback);
                    } else {
                        callback.onError(errorMsg, errorTh);
                    }
                }
            }.execute();
        } else {
            RealmUtils.removeBookmark(realm, bookmarkId, callback);
        }
    }
}
