/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

import io.github.nfdz.savedio.model.Bookmark;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * This class is a realm recycler view adapter and manage the creation and binding of bookmark UI items.
 */
public class BookmarksAdapter extends RealmRecyclerViewAdapter<Bookmark, BookmarksAdapter.BookmarksViewHolder> {

    private final Context mContext;
    private final BookmarkOnClickHandler mClickHandler;

    /**
     * The interface to be implemented to receive on click events.
     */
    public interface BookmarkOnClickHandler {
        void onFavoriteClick(Bookmark bookmark);
        void onBookmarkClick(Bookmark bookmark);
        void onLongBookmarkClick(Bookmark bookmark);
    }

    /**
     * Constructor.
     * @param context
     * @param bookmarks
     * @param clickHandler
     */
    public BookmarksAdapter(@NonNull Context context,
                            @Nullable OrderedRealmCollection<Bookmark> bookmarks,
                            @Nullable BookmarkOnClickHandler clickHandler) {
        super(context, bookmarks, true);
        mContext = context;
        mClickHandler = clickHandler;
    }

    @Override
    public BookmarksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = R.layout.bookmark_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParent = false;
        View view = inflater.inflate(layoutId, parent, shouldAttachToParent);
        return new BookmarksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookmarksViewHolder holder, int position) {
        Bookmark bookmark = getItem(position);
        holder.mBookmarkName.setText(bookmark.getTitle());
        Drawable favoriteDrawable = bookmark.isFavorite() ?
                ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_on)
              : ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_off);
        holder.mFavoriteButton.setImageDrawable(favoriteDrawable);
        String faviconPath = getFaviconPath(bookmark.getUrl());
        // add no poster art meanwhile Picasso is loading the poster
        Drawable noFavicon = ContextCompat.getDrawable(mContext, R.drawable.art_no_favicon);
        if (faviconPath != null) {
            Picasso.with(mContext)
                    .load(faviconPath)
                    .placeholder(noFavicon)
                    .into(holder.mBookmarkFavicon);
        } else {
            holder.mBookmarkFavicon.setImageDrawable(noFavicon);
        }
        boolean isTheLastOne = position == getItemCount() - 1;
        holder.mSeparator.setVisibility(isTheLastOne ? View.INVISIBLE : View.VISIBLE);
        holder.itemView.setTag(bookmark.getId());
    }

    /**
     * Tries to infer the favicon path with given URL.
     * @param rawUrl path.
     * @return inferred favicon path or null.
     */
    private String getFaviconPath(String rawUrl) {
        String faviconPath = null;
        try {
            URL url = new URL(rawUrl);
            String basePath = url.getProtocol() + "://" + url.getHost();
            faviconPath = basePath + "/favicon.ico";
        } catch (MalformedURLException e) {
            // swallow
        }
        return faviconPath;
    }

    /**
     * Cache of the children views for a bookmark list item.
     */
    public class BookmarksViewHolder extends RecyclerView.ViewHolder {

        public final TextView mBookmarkName;
        public final ImageView mBookmarkFavicon;
        public final View mSeparator;
        private final ImageView mFavoriteButton;

        public BookmarksViewHolder(View view) {
            super(view);
            mBookmarkName = (TextView) view.findViewById(R.id.tv_bookmark_item_name);
            mBookmarkFavicon = (ImageView) view.findViewById(R.id.iv_bookmark_item_favicon);
            mFavoriteButton = (ImageView) view.findViewById(R.id.iv_bookmark_item_favorite);
            mSeparator = view.findViewById(R.id.bookmark_item_separator);

            // register listeners
            mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    Bookmark bookmark = getItem(adapterPosition);
                    if (mClickHandler != null) mClickHandler.onFavoriteClick(bookmark);
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    Bookmark bookmark = getItem(adapterPosition);
                    if (mClickHandler != null) mClickHandler.onBookmarkClick(bookmark);
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    Bookmark bookmark = getItem(adapterPosition);
                    if (mClickHandler != null) mClickHandler.onLongBookmarkClick(bookmark);
                    return true;
                }
            });
        }
    }
}
