/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;

import io.github.nfdz.savedio.data.PreferencesUtils;
import io.github.nfdz.savedio.data.RealmUtils;
import io.github.nfdz.savedio.sync.SyncUtils;
import io.github.nfdz.savedio.utils.ImportExportUtils;
import io.realm.Realm;
import timber.log.Timber;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private Realm mRealm;

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

        Preference importPref = findPreference(getString(R.string.pref_import_key));
        Preference exportPref = findPreference(getString(R.string.pref_export_key));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            importPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ImportExportUtils.importBookmarks(SettingsFragment.this);
                    return true;
                }
            });
            exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ImportExportUtils.exportBookmarks(SettingsFragment.this);
                    return true;
                }
            });
        } else {
            importPref.setSummary(R.string.pref_import_summary_unavailable);
            importPref.setEnabled(false);
            exportPref.setSummary(R.string.pref_export_summary_unavailable);
            exportPref.setEnabled(false);
        }
    }

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

    private boolean shouldSetSummary(Preference p) {
        return !(p instanceof CheckBoxPreference) &&
                !p.getKey().equals(getString(R.string.pref_api_key)) &&
                !p.getKey().equals(getString(R.string.pref_export_key)) &&
                !p.getKey().equals(getString(R.string.pref_import_key));
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(getContext());
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
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
            boolean smartFavs = sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_smart_default));
            updateFavorites(mRealm, smartFavs);
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
                            Timber.i(th, "There was an error computing smart favorites. " + msg);
                        }
                    });
                }
            }
            @Override
            public void onError(String msg, Throwable th) {
                Timber.i(th, "There was an error clearing favorites. " + msg);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (!ImportExportUtils.onImportActivityResult(requestCode, resultCode, resultData, mRealm, getContext()) &&
            !ImportExportUtils.onExportActivityResult(requestCode, resultCode, resultData, mRealm, getContext())) {
            super.onActivityResult(requestCode, resultCode, resultData);
        }
    }
}