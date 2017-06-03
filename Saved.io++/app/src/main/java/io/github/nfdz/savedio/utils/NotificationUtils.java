/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import java.util.List;

import io.github.nfdz.savedio.MainActivity;
import io.github.nfdz.savedio.R;

public class NotificationUtils {

    private static final int BOOKMARK_LIST_NOTIFICATION_ID = 2468;

    /**
     * Notifies to user one notification with information about updates in given lists.
     * @param context
     * @param listsToNotify
     */
    public static void notifyListChanges(Context context, List<String> listsToNotify) {
        if (listsToNotify.isEmpty()) return;
        String notificationTitle = context.getString(R.string.app_name);
        String notificationTextShort;
        String notificationTextLong;
        if (listsToNotify.size() == 1) {
            String listName = listsToNotify.get(0);
            notificationTextLong = String.format(context.getString(R.string.notification_list_format_long), listName);
            notificationTextShort = String.format(context.getString(R.string.notification_list_format_short), listName);
        } else {
            notificationTextLong = context.getString(R.string.notification_several_lists_long);
            notificationTextShort = context.getString(R.string.notification_several_lists_short);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_logo_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationTextShort)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationTextLong))
                .setAutoCancel(true);

        Intent listIntent = new Intent(context, MainActivity.class);
        // if there is only one list with changes, open main activity with this content list
        if (listsToNotify.size() == 1) {
            String listName = listsToNotify.get(0);
            listIntent.putExtra(MainActivity.LIST_KEY, listName);
        }

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(listIntent);
        PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(BOOKMARK_LIST_NOTIFICATION_ID, notificationBuilder.build());
    }
}
