/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.widget;


import android.content.Context;
import android.content.Intent;

public class WidgetUtils {

    public static final String ACTION_FAV_DATA_UPDATED = "io.github.nfdz.savedio.ACTION_FAV_DATA_UPDATED";

    public static void updateFavWidgets(Context context) {
        // setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_FAV_DATA_UPDATED).setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}
