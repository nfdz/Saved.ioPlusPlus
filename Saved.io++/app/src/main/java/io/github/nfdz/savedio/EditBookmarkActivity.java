/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.savedio.data.RealmUtils;
import io.github.nfdz.savedio.model.Bookmark;
import io.github.nfdz.savedio.utils.BookmarkFormUtils;
import io.github.nfdz.savedio.utils.TasksUtils;
import io.github.nfdz.savedio.utils.ToolbarUtils;
import io.realm.Realm;

/**
 * This activity shows bookmark layout form filled with given bookmark (in intent). If user
 * clicks edit button, it will remove old bookmark and create a new one.
 */
public class EditBookmarkActivity extends AppCompatActivity {

    /** Key of the bookmark ID in extra data intent map */
    public static final String BOOKMARK_ID_KEY = "bookmark-id";

    private static final String TAG = EditBookmarkActivity.class.getName();
    private static final String NO_LIST_VALUE = "-";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.toolbar_logo) ImageView mLogo;
    @BindView(R.id.pb_edit_bookmark_loading) ProgressBar mLoading;
    @BindView(R.id.et_bookmark_form_title) EditText mBookmarkTitle;
    @BindView(R.id.et_bookmark_form_url) EditText mBookmarkUrl;
    @BindView(R.id.et_bookmark_form_notes) EditText mBookmarkNotes;
    @BindView(R.id.tv_bookmark_form_list) TextView mBookmarkList;
    @BindView(R.id.layout_edit_bookmark_content) View mContent;
    @BindView(R.id.button_bookmark_form_action) Button mEditButton;

    private Realm mRealm;
    private List<String> mAvailableLists = new ArrayList<>();
    private TextWatcher mUrlValidator;
    private String mBookmarkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_edit_bookmark);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        ToolbarUtils.setUpActionBar(getSupportActionBar());
        Realm.init(this);
        mRealm = Realm.getDefaultInstance();

        // extract bookmark ID from intent
        if (getIntent() != null && getIntent().hasExtra(BOOKMARK_ID_KEY)) {
            mBookmarkId = getIntent().getStringExtra(BOOKMARK_ID_KEY);
        }
        if (TextUtils.isEmpty(mBookmarkId)) {
            Log.d(TAG, "Edit bookmark activity created with null bookmark id given in intent.");
            finish();
        }

        // TODO store in savedInstanceState the selected list and available lists
        mEditButton.setText(R.string.edit_bookmark_button);
        mAvailableLists.add(NO_LIST_VALUE);
        mUrlValidator = new BookmarkFormUtils.URLTextValidator(mEditButton);
        mBookmarkUrl.addTextChangedListener(mUrlValidator);
        showLoading();
        retrieveAvailableLists();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBookmarkUrl.removeTextChangedListener(mUrlValidator);
        mRealm.close();
    }

    private void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.INVISIBLE);
    }

    private void showContent() {
        mLoading.setVisibility(View.INVISIBLE);
        mContent.setVisibility(View.VISIBLE);
    }

    /**
     * Retrieves available list names from persistence and calls fill bookmark form method when
     * it is done.
     */
    private void retrieveAvailableLists() {
        RealmUtils.retrieveListNames(mRealm, new Callbacks.OperationCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                mAvailableLists.addAll(result);
                Collections.sort(mAvailableLists);
                fillBookmarkForm();
            }
            @Override
            public void onError(String msg, Throwable e) {
                Log.e(TAG, msg, e);
                fillBookmarkForm();
            }
        });
    }


    @OnClick(R.id.tv_bookmark_form_list)
    public void onChangeSelectedListClick() {
        BookmarkFormUtils.onChangeSelectedListClick(this, mAvailableLists, mBookmarkList);
    }

    @OnClick(R.id.ib_bookmark_form_add_list)
    public void onAddListClick() {
        BookmarkFormUtils.onAddListClick(this, mAvailableLists, mBookmarkList);
    }

    /**
     * Retrieves bookmark from realm persistence and fills all form fields.
     */
    private void fillBookmarkForm() {
        // retrieve bookmark
        Bookmark bookmark = mRealm.where(Bookmark.class).equalTo(Bookmark.FIELD_ID, mBookmarkId).findFirst();
        if (bookmark == null) {
            Log.d(TAG, "Edit bookmark activity created with bookmarkId=" + mBookmarkId +
                    " cannot find bookmark object.");
            finish();
            return;
        }
        // fill form
        mBookmarkTitle.setText(bookmark.getTitle());
        mBookmarkUrl.setText(bookmark.getUrl());
        mBookmarkNotes.setText(bookmark.getNotes());
        String listName = bookmark.getListName();
        if (TextUtils.isEmpty(listName)) listName = NO_LIST_VALUE;
        mBookmarkList.setText(listName);
        // ensure that list name is contained in available lists
        if (!mAvailableLists.contains(listName)) {
            mAvailableLists.add(listName);
            Collections.sort(mAvailableLists);
        }
        showContent();
    }

    @OnClick(R.id.button_bookmark_form_action)
    public void onEditClick() {
        showLoading();

        // get fields from form and set in a unmanaged bookmark object
        String title = mBookmarkTitle.getText().toString();
        String url = mBookmarkUrl.getText().toString();
        String notes = mBookmarkNotes.getText().toString();
        String list = mBookmarkList.getText().toString();
        if (list.equals(NO_LIST_VALUE)) list = "";
        final Bookmark editedBookmark = new Bookmark();
        editedBookmark.setTitle(title);
        editedBookmark.setUrl(url);
        editedBookmark.setNotes(notes);
        editedBookmark.setListName(list);

        // check that url is ok
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "URL is empty when edit button was clicked.");
        } else {
            TasksUtils.deleteBookmark(mRealm,
                    mBookmarkId,
                    new Callbacks.OperationCallback<Bookmark>() {
                        @Override
                        public void onSuccess(Bookmark removed) {
                            TasksUtils.createBookmark(mRealm,
                                    editedBookmark,
                                    new Callbacks.OperationCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void v) {
                                            finish();
                                        }
                                        @Override
                                        public void onError(String error, Throwable th) {
                                            String msg = "There was an error editing bookmark: " + editedBookmark + ". " + error;
                                            Log.e(TAG, msg, th);
                                            showContent();
                                            Toast.makeText(EditBookmarkActivity.this, msg, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                        @Override
                        public void onError(String error, Throwable th) {
                            String msg = "There was an error editing bookmark: " + editedBookmark + ". " + error;
                            Log.e(TAG, msg, th);
                            showContent();
                            Toast.makeText(EditBookmarkActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
            });
        }
    }
}
