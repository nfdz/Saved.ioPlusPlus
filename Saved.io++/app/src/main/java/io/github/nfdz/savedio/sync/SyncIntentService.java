/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.sync;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.nfdz.savedio.BuildConfig;
import io.github.nfdz.savedio.R;
import io.github.nfdz.savedio.data.PreferencesUtils;
import io.github.nfdz.savedio.model.Bookmark;
import io.github.nfdz.savedio.model.BookmarkList;
import io.github.nfdz.savedio.model.SyncResult;
import io.github.nfdz.savedio.sync.api.APIHelper;
import io.github.nfdz.savedio.sync.api.BookmarkAPI;
import io.github.nfdz.savedio.utils.NotificationUtils;
import io.github.nfdz.savedio.widget.WidgetUtils;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class SyncIntentService extends IntentService {

    public static final String SERVICE_NAME = "SyncIntentService";

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
            // TODO improve it in order to update only if a favorite bookmark was updated or removed
            WidgetUtils.updateFavWidgets(this);
        } catch (SyncException e) {
            realm.beginTransaction();
            SyncResult result = realm.where(SyncResult.class).findFirst();
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            realm.commitTransaction();
        } finally {
            if (realm != null) realm.close();
        }
    }

    private static void syncBookmarks(Context context, Realm realm) throws SyncException {

        Timber.i("Starting bookmarks synchronization.");

        //  retrieve all bookmarks
        String devKey = BuildConfig.SAVEDIO_API_DEV_KEY;
        String userKey = PreferencesUtils.getUserAPIKey(context);
        // check user key is valid
        if (TextUtils.isEmpty(userKey)) {
            throw new SyncException(context.getString(R.string.sync_api_error));
        }

        APIHelper helper = new APIHelper();
        final int limit = 50;
        final String noList = null;

        // Notes:
        // - First page is 1 because 0 returns the same response
        // - It will not returns an empty response ever, it repeats the last page.
        //   So it has to check if the last page is the same that the last one
        int page = 1;
        List<BookmarkAPI> allRetrievedBms = new ArrayList<>();
        List<BookmarkAPI> retrievedBms;
        String lastOneId = null;
        boolean exit = false;
        do {
            Call<List<BookmarkAPI>> call = helper.getAPI().retrieveAllBookmarks(devKey, userKey, page, limit, noList);
            try {
                Response<List<BookmarkAPI>> res = call.execute();
                if (res.isSuccessful()) {
                    retrievedBms = res.body();
                    if (!retrievedBms.isEmpty()) {
                        String newLastOneId = retrievedBms.get(retrievedBms.size() - 1).id;
                        if (lastOneId != null && lastOneId.equals(newLastOneId)) {
                            exit = true;
                        } else {
                            Timber.d("Sync bookmarks - page=" + page + " - size=" + retrievedBms.size());
                            page++;
                            allRetrievedBms.addAll(retrievedBms);
                            lastOneId = newLastOneId;
                        }
                    } else {
                        exit = true;
                    }
                } else {
                    String error = res.raw().message();
                    Timber.d("Sync bookmarks error (page=" + page + "): " + error);
                    throw new SyncException(context.getString(R.string.sync_service_error));
                }
            } catch (IOException e) {
                Timber.d(e, "Sync bookmarks error (page=" + page + ")");
                throw new SyncException(context.getString(R.string.sync_network_error), e);
            }
        } while(!exit);


        List<String> allRetrievedBmsId = new ArrayList<>();
        for (BookmarkAPI bm : allRetrievedBms) {
            allRetrievedBmsId.add(bm.id);
        }

        realm.beginTransaction();

        // remove local bookmarks not contained in remote server
        List<Bookmark> removedBookmarks = new ArrayList<>();
        List<Bookmark> localBookmarks = realm.where(Bookmark.class).findAll();
        for (Bookmark bookmark : localBookmarks) {
            if (!allRetrievedBmsId.contains(bookmark.getId())) {
                removedBookmarks.add(bookmark);
                bookmark.deleteFromRealm();
            }
        }

        // create or update bookmarks
        List<Bookmark> createdBookmarks = new ArrayList<>();
        List<Bookmark> updateBookmarks = new ArrayList<>();
        for (BookmarkAPI bm : allRetrievedBms) {
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
                createdBookmarks.add(bookmark);
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
                    updateBookmarks.add(bookmark);
                }
            }
        }

        // notify changes and purge empty lists
        List<String> listsToNotify = new ArrayList<>();
        List<BookmarkList> lists = realm.where(BookmarkList.class).findAll();
        for (BookmarkList list : lists) {
            if (list.getBookmarks().isEmpty()) {
                list.deleteFromRealm();
            } else if (list.getNotifyFlag()) {
                // check if it contains any new or updated bookmark
                boolean notify = false;
                for (Bookmark bookmark : createdBookmarks) {
                    if (list.getBookmarks().contains(bookmark)) {
                        notify = true;
                        break;
                    }
                }
                if (!notify) {
                    for (Bookmark bookmark : updateBookmarks) {
                        if (list.getBookmarks().contains(bookmark)) {
                            notify = true;
                            break;
                        }
                    }
                }
                if (notify) listsToNotify.add(list.getListName());
            }
        }
        NotificationUtils.notifyListChanges(context, listsToNotify);

        // create summary
        String summary = String.format(context.getString(R.string.sync_summary_format),
                removedBookmarks.size(),
                createdBookmarks.size(),
                updateBookmarks.size());

        // save sync result
        SyncResult result = realm.where(SyncResult.class).findFirst();
        result.setSuccess(true);
        result.setMessage(summary);

        // commit data
        realm.commitTransaction();

        Timber.i("Bookmarks synchronization finished correctly. " + summary);

        // save sync time in preferences
        long now = System.currentTimeMillis();
        PreferencesUtils.setLastSyncTime(context, now);
    }
}
