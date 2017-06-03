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

/**
 * This class has static methods to ease work with shared preferences
 */
public class PreferencesUtils {

    private static final String FINISHED_INTRO_KEY = "finished-intro";
    private static final boolean FINISHED_INTRO_DEFAULT = false;

    private static final String LAST_SYNC_KEY = "last-sync";
    private static final long LAST_SYNC_DEFAULT = 0L;

    /**
     * Retrieves sort preference in an asynchronous way.
     * @param context
     * @param callback
     * @return AsyncTask
     */
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

    /**
     * Retrieves sort preference in a synchronous way.
     * @param context
     * @return String sort
     */
    public static String getPreferredSort(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForSort = context.getString(R.string.pref_sort_key);
        String defaultSort = context.getString(R.string.pref_sort_default);
        return sp.getString(keyForSort, defaultSort);
    }

    /**
     * Retrieves user API key preference in a synchronous way.
     * @param context
     * @return String user API key
     */
    public static String getUserAPIKey(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForUserAPIKey = context.getString(R.string.pref_api_key);
        String defaultUserAPIKey = context.getString(R.string.pref_api_default);
        return sp.getString(keyForUserAPIKey, defaultUserAPIKey);
    }

    /**
     * Retrieves smart favorites flag preference in a synchronous way.
     * @param context
     * @return boolean smart favorites flag
     */
    public static boolean getSmartFavoritesFlag(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_smart_key);
        boolean defaultFlag = context.getResources().getBoolean(R.bool.pref_smart_default);
        return sp.getBoolean(key, defaultFlag);
    }

    /**
     * Retrieves last synchronization time in an asynchronous way.
     * @param context
     * @param callback
     * @return AsyncTask
     */
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

    /**
     * Updates last synchronization preference time with the given one in an asynchronous way.
     * @param context
     * @param lastSyncTime
     */
    public static void setLastSyncTime(Context context, long lastSyncTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(LAST_SYNC_KEY, lastSyncTime);
        editor.apply();
    }

    /**
     * Updates finished introduction flag preference in an asynchronous way.
     * @param context
     * @param finished
     */
    public static void setFinishedIntro(Context context, boolean finished) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(FINISHED_INTRO_KEY, finished);
        editor.apply();
    }

    /**
     * Retrieves finished intro flag preference in an asynchronous way.
     * @param context
     * @param callback
     * @return AsyncTask
     */
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
