/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.sync.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.nfdz.savedio.sync.api.SavedioAPI;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This class helps to manage retrofit API object.
 */
public class APIHelper {

    private static final String BASE_URL = "https://devapi.saved.io/";

    public final SavedioAPI mAPI;

    public APIHelper() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mAPI = retrofit.create(SavedioAPI.class);
    }

    public SavedioAPI getAPI() {
        return mAPI;
    }

}
