/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.model;


import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * This class contains all fields of a list.
 */
public class BookmarkList extends RealmObject {

    // strings with the name of attributes to ease realm queries
    public static final String FIELD_LIST_NAME = "mListName";
    public static final String FIELD_NOTIFY = "mNotifyFlag";

    @PrimaryKey
    private String mListName;

    private RealmList<Bookmark> mBookmarks;

    /** Notify new bookmarks flag */
    private boolean mNotifyFlag;

    public boolean getNotifyFlag() {
        return mNotifyFlag;
    }

    public void setNofityFlag(boolean notifyFlag) {
        mNotifyFlag = notifyFlag;
    }

    public String getListName() {
        return mListName;
    }

    public void setListName(String listName) {
        mListName = listName;
    }

    public RealmList<Bookmark> getBookmarks() {
        return mBookmarks;
    }

    public void setBookmarks(RealmList<Bookmark> bookmarks) {
        mBookmarks = bookmarks;
    }
}
