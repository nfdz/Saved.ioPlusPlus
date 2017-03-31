/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * This class contains all fields of a bookmark.
 */
public class Bookmark extends RealmObject {

    // strings with the name of attributes to ease realm queries
    public static final String FIELD_ID = "mId";
    public static final String FIELD_URL = "mUrl";
    public static final String FIELD_TITLE = "mTitle";
    public static final String FIELD_NOTE = "mNotes";
    public static final String FIELD_DATE = "mDate";
    public static final String FIELD_LIST = "mListName";

    @PrimaryKey
    private String mId;

    private String mUrl;

    private String mTitle;

    private String mNotes;

    private String mDate;

    private String mListName;

    public void setId(String id) {
        mId = id;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setListName(String listName) {
        mListName = listName;
    }

    public String getId() {
        return mId;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getNotes() {
        return mNotes;
    }

    public String getDate() {
        return mDate;
    }

    public String getListName() {
        return mListName;
    }
}
