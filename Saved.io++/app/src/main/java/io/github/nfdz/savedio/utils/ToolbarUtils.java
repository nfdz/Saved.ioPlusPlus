/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.utils;


import android.support.v7.app.ActionBar;

/**
 * This class contains static methods to ease work with application toolbar.
 */
public class ToolbarUtils {

    /**
     * This method configures given actionbar with the desired behaviour.
     * @param actionBar
     */
    public static void setUpActionBar(ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(null);
    }

}
