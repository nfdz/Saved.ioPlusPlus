/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.utils;


import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

/**
 * This class contains static methods to ease work with application toolbar.
 */
public class ToolbarUtils {

    /**
     * This method centers given logo in the middle of the given toolbar.
     * @param toolbar
     * @param logo
     */
    public static void centerLogo(Toolbar toolbar, ImageView logo) {
        int offset = (toolbar.getWidth() / 2) - (logo.getWidth() / 2);
        logo.setX(offset);
        logo.setVisibility(View.VISIBLE);
    }

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
