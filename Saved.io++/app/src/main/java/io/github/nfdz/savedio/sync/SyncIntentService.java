/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.sync;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.nfdz.savedio.BuildConfig;
import io.github.nfdz.savedio.data.PreferencesUtils;
import io.github.nfdz.savedio.model.Bookmark;
import io.github.nfdz.savedio.model.BookmarkList;
import io.github.nfdz.savedio.sync.api.APIHelper;
import io.github.nfdz.savedio.sync.api.BookmarkAPI;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;

public class SyncIntentService extends IntentService {

    public static final String SERVICE_NAME = "SyncIntentService";

    public static final String SUMMARY_FORMAT = "Changes: %d removed, %d created and %d updated.";

    public static final String TAG = SyncIntentService.class.getSimpleName();

    public SyncIntentService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Realm realm = null;
        try {
            Realm.init(this);
            realm = Realm.getDefaultInstance();
            syncBookmarks(this, realm);
        } finally {
            if (realm != null) realm.close();
        }
    }

    private static void syncBookmarks(Context context, Realm realm) {

        //  retrieve all bookmarks
        String devKey = BuildConfig.SAVEDIO_API_DEV_KEY;
        String userKey = PreferencesUtils.getUserAPIKey(context);
        // check user key is valid
        if (TextUtils.isEmpty(userKey)) {
            // not valid
            return;
        }

        APIHelper helper = new APIHelper();
        Call<List<BookmarkAPI>> call = helper.getAPI().retrieveAllBookmarks(devKey,userKey, null,null,null);
        List<BookmarkAPI> retrievedBookmarks = null;
        try {
            Response<List<BookmarkAPI>> res = call.execute();
            if (res.isSuccessful()) {
                retrievedBookmarks = res.body();
            } else {
                String error = res.raw().message();
                Log.d(TAG, "Sync bookmarks error: " + error);
                // TODO
                return;
            }
        } catch (IOException e) {
            Log.d(TAG, "Sync bookmarks error: " + e.getMessage(), e);
            // TODO
            return;
        }

        List<String> retrievedBookmarksId = new ArrayList<>();
        for (BookmarkAPI bm : retrievedBookmarks) {
            retrievedBookmarksId.add(bm.id);
        }

        realm.beginTransaction();

        // remove local bookmarks not contained in remote server
        List<String> removedBookmarks = new ArrayList<>();
        List<Bookmark> localBookmarks = realm.where(Bookmark.class).findAll();
        for (Bookmark bookmark : localBookmarks) {
            if (!retrievedBookmarksId.contains(bookmark.getId())) {
                removedBookmarks.add(bookmark.getId());
                bookmark.deleteFromRealm();
            }
        }

        // create or update bookmarks
        List<String> createdBookmarks = new ArrayList<>();
        List<String> updateBookmarks = new ArrayList<>();
        for (BookmarkAPI bm : retrievedBookmarks) {
            Bookmark bookmark = realm.where(Bookmark.class)
                    .equalTo(Bookmark.FIELD_ID, bm.id)
                    .findFirst();
            if (bookmark == null) {
                // create a new bookmark
                bookmark = realm.createObject(Bookmark.class, bm.id);
                bookmark.setTitle(bm.title);
                bookmark.setDate(bm.date);
                bookmark.setUrl(bm.url);
                bookmark.setNotes(bm.note);
                createdBookmarks.add(bm.id);
            } else {
                boolean isEqual = bookmark.getTitle().equals(bm.title) &&
                                  bookmark.getDate().equals(bm.date) &&
                                  bookmark.getUrl().equals(bm.url) &&
                                  bookmark.getNotes().equals(bm.note);
                if (!isEqual) {
                    bookmark.setTitle(bm.title);
                    bookmark.setDate(bm.date);
                    bookmark.setUrl(bm.url);
                    bookmark.setNotes(bm.note);
                    updateBookmarks.add(bm.id);
                }
            }
        }

        // purge empty lists
        List<BookmarkList> lists = realm.where(BookmarkList.class).findAll();
        for (BookmarkList list : lists) {
            if (list.getBookmarks().isEmpty()) list.deleteFromRealm();
        }

        // commit data
        realm.commitTransaction();

        String summary = String.format(SUMMARY_FORMAT, removedBookmarks.size(), createdBookmarks.size(), updateBookmarks.size());
        Log.i(TAG, "Sync bookmarks finished correctly. " + summary);

        // save sync time in preferences
        long now = System.currentTimeMillis();
        PreferencesUtils.setLastSyncTime(context, now);
    }
}
