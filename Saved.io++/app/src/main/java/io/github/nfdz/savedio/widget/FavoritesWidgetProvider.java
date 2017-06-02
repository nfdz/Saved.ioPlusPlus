/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import io.github.nfdz.savedio.MainActivity;
import io.github.nfdz.savedio.R;

public class FavoritesWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_favorites);

            // create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_fav_bar, pendingIntent);

            // set logo programmatically to avoid problems with vectors, use bitmap only if it is under 21
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                views.setImageViewResource(R.id.widget_fav_bar_logo, R.drawable.ic_logo_png);
            } else {
                views.setImageViewResource(R.id.widget_fav_bar_logo, R.drawable.ic_logo);
            }

            // set up the collection
            views.setRemoteAdapter(R.id.widget_fav_list, new Intent(context, FavoritesWidgetRemoteViewsService.class));

            // create template intent for list item
            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            PendingIntent clickPendingIntentTemplate = PendingIntent.getActivity(context, 0, openIntent, 0);
            views.setPendingIntentTemplate(R.id.widget_fav_list, clickPendingIntentTemplate);

            views.setEmptyView(R.id.widget_fav_list, R.id.widget_fav_empty);

            // tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (WidgetUtils.ACTION_FAV_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, FavoritesWidgetProvider.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_fav_list);
        }
    }
}
