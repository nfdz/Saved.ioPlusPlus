/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.sync;


import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import io.github.nfdz.savedio.Callbacks;
import io.github.nfdz.savedio.data.PreferencesUtils;

public class SyncFirebaseJobService extends JobService {

    private AsyncTask<Void, Void, Void> mSyncTask;

    @Override
    public boolean onStartJob(JobParameters job) {
        mSyncTask = PreferencesUtils.retrieveLastSyncTime(this, new Callbacks.FinishCallback<Long>() {
            @Override
            public void onFinish(Long lastSync) {
                long now = System.currentTimeMillis();
                if (now - lastSync > SyncUtils.SYNC_INTERVAL_MILLIS) {
                    SyncUtils.startImmediateSync(SyncFirebaseJobService.this);
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
