/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.sync.api;

import com.google.gson.annotations.SerializedName;

/**
 * POJO class with attributes and names defined by API.
 */
public class BookmarkAPI {

    /**
     * Date format of date stored in "date" variable. For example: 2017-03-02 13:08:41
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @SerializedName("bk_id")
    public String id;

    @SerializedName("bk_url")
    public String url;

    @SerializedName("bk_title")
    public String title;

    @SerializedName("bk_note")
    public String note;

    @SerializedName("bk_date")
    public String date;
}