/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.nfdz.savedio.model.BookmarkList;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * This class is a realm adapter and manage the creation and binding of list UI items.
 */
public class ListsAdapter extends RealmBaseAdapter<BookmarkList> {

    private final Context mContext;

    /**
     * Constructor.
     * @param context
     * @param data
     */
    public ListsAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<BookmarkList> data) {
        super(context, data);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null || convertView instanceof TextView) {
            view = convertView;
        } else {
            int layoutId = R.layout.nav_main_menu_item;
            LayoutInflater inflater = LayoutInflater.from(mContext);
            boolean shouldAttachToParent = false;
            view = inflater.inflate(layoutId, parent, shouldAttachToParent);
        }
        ((TextView)view).setText(getItem(position).getListName());
        return view;
    }
}
