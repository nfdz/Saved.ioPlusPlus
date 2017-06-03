/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.sync;


import android.os.AsyncTask;
import android.text.TextUtils;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import io.github.nfdz.savedio.Callbacks;
import io.github.nfdz.savedio.R;
import io.github.nfdz.savedio.data.PreferencesUtils;

/**
 * Synchronization firebase job service implementation.
 */
public class SyncFirebaseJobService extends JobService {

    private AsyncTask<Void, Void, Void> mSyncTask;

    @Override
    public boolean onStartJob(JobParameters job) {
        // check if synchronization is necessary
        mSyncTask = PreferencesUtils.retrieveLastSyncTime(this, new Callbacks.FinishCallback<Long>() {
            @Override
            public void onFinish(Long lastSync) {
                long now = System.currentTimeMillis();
                if (now - lastSync > SyncUtils.SYNC_INTERVAL_MILLIS) {
                    // check if app is in offline mode (no user API key)
                    String userKey = PreferencesUtils.getUserAPIKey(SyncFirebaseJobService.this);
                    if (!TextUtils.isEmpty(userKey)) {
                        SyncUtils.startImmediateSync(SyncFirebaseJobService.this);
                    }
                }
            }
        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (mSyncTask != null) mSyncTask.cancel(true);
        return true;
    }
}
