/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.model;

import java.util.Comparator;

/**
 * Date comparator. The order is from oldest to newest.
 * It is not necessary to create date time objects since the serialized string format of the date
 * facilitates the work by making the order exactly the same.
 */
public class BookmarkDateComparator implements Comparator<Bookmark> {
    @Override
    public int compare(Bookmark bookmark1, Bookmark bookmark2) {
        if (bookmark1.isFavorite() && !bookmark2.isFavorite()) {
            return -1;
        } else if (!bookmark1.isFavorite() && bookmark2.isFavorite()) {
            return 1;
        } else {
            return bookmark1.getDate().compareTo(bookmark2.getDate());
        }
    }
}