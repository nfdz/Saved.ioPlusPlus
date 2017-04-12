/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.sync.api;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SavedioAPI {

    /**
     * Returns the most recent bookmarks at a time.
     * @param devKey Mandatory.
     * @param userKey Mandatory.
     * @param page Optional. Default is 1.
     * @param limit Optional. Default is 50.
     * @param list Optional.
     * @return
     */
    @GET("bookmarks/")
    Call<List<BookmarkAPI>> retrieveAllBookmarks(@Query("devkey") String devKey,
                                                 @Query("key") String userKey,
                                                 @Query("page") Integer page,
                                                 @Query("limit") Integer limit,
                                                 @Query("list") String list);


    /**
     * Returns a single bookmark when given the bookmark ID.
     * @param devKey Mandatory.
     * @param userKey Mandatory.
     * @param bookmarkId Mandatory.
     * @return
     */
    @GET("bookmarks/{id}")
    Call<BookmarkAPI> retrieveSingleBookmark(@Path("id") String bookmarkId,
                                             @Query("devkey") String devKey,
                                             @Query("key") String userKey);


    /**
     * @param devKey Mandatory.
     * @param userKey Mandatory.
     * @param url Mandatory.
     * @param title Mandatory.
     * @param list Optional.
     * @return the id of the newly created bookmark.
     */
    @POST("bookmarks/")
    Call<CreateBookmarkResponse> createBookmark(@Query("devkey") String devKey,
                                                @Query("key") String userKey,
                                                @Query("url") String url,
                                                @Query("title") String title,
                                                @Query("list") String list);

    /**
     * @param devKey Mandatory.
     * @param userKey Mandatory.
     * @param bookmarkId Mandatory.
     * @return
     */
    @DELETE("bookmarks/")
    Call<Void> deleteBookmark(@Query("devkey") String devKey,
                              @Query("key") String userKey,
                              @Query("id") String bookmarkId);
}
