/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.sync;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import io.github.nfdz.savedio.Callbacks;
import io.github.nfdz.savedio.data.PreferencesUtils;
import io.github.nfdz.savedio.sync.api.APIHelper;

public class SyncUtils {

    private static final long SYNC_INTERVAL_HOURS = 24;
    private static final long SYNC_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final long SYNC_INTERVAL_MILLIS = TimeUnit.HOURS.toMillis(SYNC_INTERVAL_HOURS);
    private static final long SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

    private static final String SYNC_TAG = "savediopp-sync";

    private static boolean sInitialized = false;

    synchronized public static void initialize(@NonNull final Context context) {

        if (sInitialized) return;
        sInitialized = true;

        //scheduleFirebaseJobDispatcherSync(context);

        PreferencesUtils.retrieveLastSyncTime(context, new Callbacks.FinishCallback<Long>() {
            @Override
            public void onFinish(Long lastSync) {
                long now = System.currentTimeMillis();
                if (now - lastSync > SYNC_INTERVAL_MILLIS) {
                    startImmediateSync(context);
                }
            }
        });
    }

    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSync = new Intent(context, SyncIntentService.class);
        context.startService(intentToSync);
    }

//    static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context) {
//
//        Driver driver = new GooglePlayDriver(context);
//        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
//
//        Job syncSunshineJob = dispatcher.newJobBuilder()
//                .setService(SyncFirebaseJobService.class)
//                .setTag(SYNC_TAG)
//                .setConstraints(Constraint.ON_ANY_NETWORK)
//                .setLifetime(Lifetime.FOREVER)
//                .setRecurring(true)
//                .setTrigger(Trigger.executionWindow(
//                        SYNC_INTERVAL_SECONDS,
//                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
//                .setReplaceCurrent(true)
//                .build();
//
//        dispatcher.schedule(syncSunshineJob);
//    }

}
