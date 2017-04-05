/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.data;


import android.telecom.Call;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.github.nfdz.savedio.Callbacks;
import io.github.nfdz.savedio.model.Bookmark;
import io.github.nfdz.savedio.model.BookmarkList;
import io.realm.Realm;
import io.realm.RealmAsyncTask;

/**
 * This class contains static methods to ease work with realm persistence.
 */
public class RealmUtils {

    /**
     * Adds a bookmark to realm asynchronously.
     * @param realm it has to be initialized.
     * @param bookmark unmanaged object that contains all information.
     * @param callback to be notified.
     * @return realm async task.
     */
    public static RealmAsyncTask addBookmark(final Realm realm,
                                             final Bookmark bookmark,
                                             final Callbacks.OperationCallback<Void> callback) {
        return realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Bookmark managedBookmark = realm.copyToRealm(bookmark);
                String listName = managedBookmark.getListName();
                if (!TextUtils.isEmpty(listName)) {
                    BookmarkList list = realm.where(BookmarkList.class).equalTo(BookmarkList.FIELD_LIST_NAME, listName).findFirst();
                    // ensure that this list exists in realm
                    if (list == null) {
                        list = realm.createObject(BookmarkList.class, listName);
                    }
                    list.getBookmarks().add(managedBookmark);
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onSuccess(null);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable e) {
                callback.onError("There was an error removing", e);
            }
        });
    }

    /**
     * Cancels realm async task if it is not null and is not cancelled.
     * @param asyncTransaction
     */
    public static void cancelAsyncTransaction(RealmAsyncTask asyncTransaction) {
        if (asyncTransaction != null && !asyncTransaction.isCancelled()) {
            asyncTransaction.cancel();
        }
    }

    /**
     * Removes a bookmark from realm asynchronously.
     * @param realm it has to be initialized.
     * @param bookmarkId id that will be removed.
     * @param callback to be notified, it will return a copy of bookmark object when success.
     * @return realm async task.
     */
    public static RealmAsyncTask removeBookmark(Realm realm,
                                                final String bookmarkId,
                                                final Callbacks.OperationCallback<Bookmark> callback) {
        final AtomicReference<Bookmark> removedBookmark = new AtomicReference<>(null);
        return realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Bookmark bookmark = realm.where(Bookmark.class)
                    .equalTo(Bookmark.FIELD_ID, bookmarkId)
                    .findFirst();
                Bookmark bookmarkToRemove = realm.copyFromRealm(bookmark);
                String listName = bookmark.getListName();
                bookmark.deleteFromRealm();
                // if it was contained in a list, ensure that this list is not empty
                if (!TextUtils.isEmpty(listName)) {
                    BookmarkList list = realm.where(BookmarkList.class).equalTo(BookmarkList.FIELD_LIST_NAME, listName).findFirst();
                    if (list != null && list.getBookmarks().size() == 0) {
                        list.deleteFromRealm();
                    }
                }
                removedBookmark.set(bookmarkToRemove);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onSuccess(removedBookmark.get());
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable e) {
                callback.onError("There was an error removing", e);
            }
        });
    }

    /**
     * Retrieves the names of bookmark lists asynchronously.
     * @param realm it has to be initialized.
     * @param callback to be notified, it will return a list of strings when success.
     * @return realm async task.
     */
    public static RealmAsyncTask retrieveListNames(Realm realm,
                                                   final Callbacks.OperationCallback<List<String>> callback) {
        final List<String> lists = new ArrayList<>();
        return realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                List<BookmarkList> bookmarkLists = realm.where(BookmarkList.class).findAll();
                for (BookmarkList list : bookmarkLists) {
                    lists.add(list.getListName());
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onSuccess(lists);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable e) {
                callback.onError("There was an error retrieving bookmark lists.", e);
            }
        });
    }

    public static RealmAsyncTask setFavorite(Realm realm,
                                             final String bookmarkId,
                                             final boolean isFavorite,
                                             final Callbacks.OperationCallback<Void> callback) {
        return realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Bookmark bookmark = realm.where(Bookmark.class)
                        .equalTo(Bookmark.FIELD_ID, bookmarkId)
                        .findFirst();
                bookmark.setFavorite(isFavorite);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onSuccess(null);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable e) {
                callback.onError("There was an error modifying bookmark favorite flag.", e);
            }
        });
    }

}
