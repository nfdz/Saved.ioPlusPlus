/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.github.nfdz.savedio.R;
import io.github.nfdz.savedio.data.PreferencesUtils;
import io.github.nfdz.savedio.model.Bookmark;
import io.github.nfdz.savedio.model.BookmarkDateComparator;
import io.github.nfdz.savedio.model.BookmarkDateLastComparator;
import io.github.nfdz.savedio.model.BookmarkTitleComparator;
import io.github.nfdz.savedio.utils.URLUtils;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FavoritesWidgetRemoteViewsService extends RemoteViewsService {

    private List<Bookmark> getData() {
        Realm realm = null;
        try {
            Realm.init(this);
            realm = Realm.getDefaultInstance();

            // query data
            RealmResults<Bookmark> result = realm.where(Bookmark.class)
                    .equalTo(Bookmark.FIELD_FAVORITE, true)
                    .findAll();

            // get preferred sort
            Comparator<Bookmark> comparator;
            String sort = PreferencesUtils.getPreferredSort(this);
            if (getString(R.string.pref_sort_date_last_key).equals(sort)) {
                comparator = new BookmarkDateLastComparator();
            } else if (getString(R.string.pref_sort_date_old_key).equals(sort)) {
                comparator = new BookmarkDateComparator();
            } else {
                comparator = new BookmarkTitleComparator();
            }

            List<Bookmark> data = new ArrayList<>();
            for (Bookmark bm : result) {
                data.add(realm.copyFromRealm(bm));
            }
            Collections.sort(data, comparator);
            data = Collections.unmodifiableList(data);
            return data;
        } catch (Exception e) {
            Timber.e(e, "There was an error retrieving data for widget.");
        } finally {
            if (realm != null) realm.close();
        }
        return null;
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private List<Bookmark> data = null;

            @Override
            public void onCreate() {
                // nothing to do
            }

            @Override
            public void onDataSetChanged() {
                final long identityToken = Binder.clearCallingIdentity();
                data = getData();
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                data = null;
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.size();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || data == null) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_favorites_list_item);
                Bookmark bookmark = data.get(position);
                String faviconPath = URLUtils.getFaviconPath(bookmark.getUrl());
                Bitmap favicon = null;
                if (faviconPath != null) {
                    try {
                        favicon = Picasso.with(FavoritesWidgetRemoteViewsService.this)
                                .load(faviconPath)
                                .get();
                        views.setImageViewBitmap(R.id.widget_iv_bookmark_item_favicon, favicon);
                    } catch (IOException e) {
                        // swallow
                    }
                }
                if (favicon == null) {
                    // use bitmap only if it is under 21
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        views.setImageViewResource(R.id.widget_iv_bookmark_item_favicon, R.drawable.art_no_favicon_png);
                    } else {
                        views.setImageViewResource(R.id.widget_iv_bookmark_item_favicon, R.drawable.art_no_favicon);
                    }
                }

                views.setTextViewText(R.id.widget_tv_bookmark_item_name, bookmark.getTitle());

                String url = URLUtils.processURL(bookmark.getUrl());
                final Intent fillInIntent = new Intent();
                fillInIntent.setData(Uri.parse(url));
                views.setOnClickFillInIntent(R.id.widget_fav_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_favorites_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }
}
