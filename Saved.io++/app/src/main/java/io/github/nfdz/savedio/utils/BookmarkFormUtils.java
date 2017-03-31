/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.utils;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Collections;
import java.util.List;

/**
 * This class contains static methods to ease work with bookmark form layout.
 */
public class BookmarkFormUtils {

    /**
     * This method manages the event of change selected list button click. It offers all available
     * lists to user (with a dialog) and sets the selected value in the view.
     * @param context
     * @param availableLists
     * @param bookmarkList
     */
    public static void onChangeSelectedListClick(Context context,
                                                 List<String> availableLists,
                                                 final Button bookmarkList) {
        // find position of selected
        final String[] options = availableLists.toArray(new String[availableLists.size()]);
        String selectedList = bookmarkList.getText().toString();
        int selectedListPos = -1;
        if (selectedList != null && !selectedList.isEmpty()) {
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(selectedList)) {
                    selectedListPos = i;
                    break;
                }
            }
        }
        // ask user new selection
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select list");
        final int selected = selectedListPos;
        builder.setSingleChoiceItems(options, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int selection) {
                dialog.cancel();
                if (selection != selected) {
                    bookmarkList.setText(options[selection]);
                }
            }
        });
        builder.show();
    }

    /**
     * This method manages the event of add a new list button click. It asks the name of the new
     * list (with a dialog) and sets as the selected value.
     * @param context
     * @param availableLists
     * @param bookmarkList
     */
    public static void onAddListClick(final Context context,
                                      final List<String> availableLists,
                                      final Button bookmarkList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add a new list");
        final EditText input = new EditText(context);
        // center input edit text if it is possible
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String list = input.getText().toString().toLowerCase();
                if (checkValidListName(context, list)) {
                    if (!availableLists.contains(list)) {
                        availableLists.add(list);
                        Collections.sort(availableLists);
                    }
                    bookmarkList.setText(list);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Checks if given list name is valid. If it is not valid, it will notify to the user with
     * a dialog.
     * @param context
     * @param listName
     * @return true if it is valid, false if not.
     */
    private static boolean checkValidListName(Context context, String listName) {
        boolean isEmpty = listName == null || listName.isEmpty();
        if (isEmpty) {
            showCreateListError(context, "List name must not be empty");
            return false;
        }
        if (!checkCharsListName(listName)) {
            showCreateListError(context, "List name must contains only alphabetic (a-z) and numeric (0-9) characters");
            return false;
        }
        return true;
    }

    private static boolean checkCharsListName(String listName) {
        for (char l : listName.toCharArray()) {
            if (l >= 'a' && l <= 'z') continue;
            if (l >= '0' && l <= '9') continue;
            return false;
        }
        return true;
    }

    private static void showCreateListError(Context context, String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(msg);
        builder1.setCancelable(false);
        builder1.setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    /**
     * This text watcher implementation enable or disable given view depending if text is empty.
     */
    public static class URLTextValidator implements TextWatcher {

        private final View mTarget;

        public URLTextValidator(View target) {
            mTarget = target;
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mTarget.setEnabled(!TextUtils.isEmpty(charSequence));
        }
    }
}
