/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.preference.PreferenceManager;

import io.github.nfdz.savedio.Callbacks;
import io.github.nfdz.savedio.R;

public class PreferencesUtils {

    private static final String FINISHED_INTRO_KEY = "finished-intro";
    private static final boolean FINISHED_INTRO_DEFAULT = false;

    private static final String LAST_SYNC_KEY = "last-sync";
    private static final long LAST_SYNC_DEFAULT = 0L;

    public static AsyncTask retrievePreferredSort(final Context context,
                                                  final Callbacks.FinishCallback<String> callback) {
        return new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                String keyForSort = context.getString(R.string.pref_sort_key);
                String defaultSort = context.getString(R.string.pref_sort_default);
                return sp.getString(keyForSort, defaultSort);
            }
            @Override
            protected void onPostExecute(String pref) {
                callback.onFinish(pref);
            }
        }.execute();
    }

    public static String getUserAPIKey(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForUserAPIKey = context.getString(R.string.pref_api_key);
        String defaultUserAPIKey = context.getString(R.string.pref_api_default);
        return sp.getString(keyForUserAPIKey, defaultUserAPIKey);
    }

    public static AsyncTask retrieveLastSyncTime(final Context context,
                                                 final Callbacks.FinishCallback<Long> callback) {
        return new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... params) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                return sp.getLong(LAST_SYNC_KEY, LAST_SYNC_DEFAULT);
            }
            @Override
            protected void onPostExecute(Long pref) {
                callback.onFinish(pref);
            }
        }.execute();
    }

    public static void setLastSyncTime(Context context, long lastSyncTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(LAST_SYNC_KEY, lastSyncTime);
        editor.apply();
    }

    public static void setFinishedIntro(Context context, boolean finished) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(FINISHED_INTRO_KEY, finished);
        editor.apply();
    }

    public static AsyncTask retrieveFinishedIntro(final Context context,
                                                  final Callbacks.FinishCallback<Boolean> callback) {
        return new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                return sp.getBoolean(FINISHED_INTRO_KEY, FINISHED_INTRO_DEFAULT);
            }
            @Override
            protected void onPostExecute(Boolean pref) {
                callback.onFinish(pref);
            }
        }.execute();
    }
}
