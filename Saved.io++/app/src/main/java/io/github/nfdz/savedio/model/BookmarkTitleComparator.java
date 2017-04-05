/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.model;

import java.util.Comparator;

public class BookmarkTitleComparator implements Comparator<Bookmark> {
    @Override
    public int compare(Bookmark bookmark1, Bookmark bookmark2) {
        if (bookmark1.isFavorite() && !bookmark2.isFavorite()) {
            return -1;
        } else if (!bookmark1.isFavorite() && bookmark2.isFavorite()) {
            return 1;
        } else {
            return bookmark1.getTitle().compareTo(bookmark2.getTitle());
        }
    }
}
