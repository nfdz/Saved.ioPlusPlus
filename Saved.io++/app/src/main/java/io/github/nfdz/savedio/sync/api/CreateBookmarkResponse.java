/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.sync.api;

import com.google.gson.annotations.SerializedName;

/**
 * POJO class that contains the response of create bookmark operation.
 */
public class CreateBookmarkResponse {

    @SerializedName("bk_id")
    public String id;

    @SerializedName("bk_url")
    public String url;

    @SerializedName("bk_title")
    public String title;
}