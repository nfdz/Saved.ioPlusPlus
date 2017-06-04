/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio;

import android.app.Application;

import timber.log.Timber;

public class SavedioppApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
    }
}