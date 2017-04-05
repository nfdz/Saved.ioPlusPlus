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

}
