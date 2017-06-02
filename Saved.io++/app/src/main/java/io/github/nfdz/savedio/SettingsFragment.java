/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;

import io.github.nfdz.savedio.data.PreferencesUtils;
import io.github.nfdz.savedio.data.RealmUtils;
import io.github.nfdz.savedio.sync.SyncUtils;
import io.realm.Realm;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = SettingsFragment.class.getSimpleName();

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // look up the correct display value in
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // set the summary to the value's simple string representation
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            if (shouldSetSummary(p)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
    }

    private boolean shouldSetSummary(Preference p) {
        return !(p instanceof CheckBoxPreference) && !p.getKey().equals(getString(R.string.pref_api_key));
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_key))) {
            // nothing to do
        } else if (key.equals(getString(R.string.pref_api_key))) {
            if (!TextUtils.isEmpty(PreferencesUtils.getUserAPIKey(getContext()))) {
                SyncUtils.startImmediateSync(getContext());
            }
        } else if (key.equals(getString(R.string.pref_smart_key))) {
            Realm realm = null;
            try {
                Realm.init(getContext());
                realm = Realm.getDefaultInstance();
                boolean smartFavs = sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_smart_default));
                updateFavorites(realm, smartFavs);
            } finally {
                if (realm != null) realm.close();
            }
        }

        Preference preference = findPreference(key);
        if (preference != null) {
            if (shouldSetSummary(preference)) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            }
        }
    }

    private void updateFavorites(final Realm realm, final boolean smartFavs) {
        RealmUtils.clearFavorites(getContext(), realm, new Callbacks.OperationCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (smartFavs) {
                    RealmUtils.markSmartFavorites(getContext(), realm, new Callbacks.OperationCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            // nothing to do
                        }
                        @Override
                        public void onError(String msg, Throwable th) {
                            Log.i(TAG, "There was an error computing smart favorites. " + msg, th);
                        }
                    });
                }
            }
            @Override
            public void onError(String msg, Throwable th) {
                Log.i(TAG, "There was an error clearing favorites. " + msg, th);
            }
        });
    }
}