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
 * This activity shows an empty bookmark layout form and manage its creation.
 */
public class NewBookmarkActivity extends AppCompatActivity {

    /** Key of the selected list in extra data intent map */
    public static final String SELECTED_LIST_KEY = "selected-list";

    private static final String TAG = NewBookmarkActivity.class.getName();
    private static final String NO_LIST_VALUE = "-";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.toolbar_logo) ImageView mLogo;
    @BindView(R.id.et_bookmark_form_title) EditText mBookmarkTitle;
    @BindView(R.id.et_bookmark_form_url) EditText mBookmarkUrl;
    @BindView(R.id.et_bookmark_form_notes) EditText mBookmarkNotes;
    @BindView(R.id.pb_new_bookmark_loading) ProgressBar mSaveLoading;
    @BindView(R.id.button_bookmark_form_list) Button mBookmarkList;
    @BindView(R.id.layout_new_bookmark_content) View mContent;
    @BindView(R.id.button_bookmark_form_action) Button mSaveButton;

    private Realm mRealm;
    private List<String> mAvailableLists = new ArrayList<>();
    private TextWatcher mUrlValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_new_bookmark);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        ToolbarUtils.setUpActionBar(getSupportActionBar());
        Realm.init(this);
        mRealm = Realm.getDefaultInstance();

        // extract initial selection from intent
        String initialSelection = null;
        if (getIntent() != null && getIntent().hasExtra(SELECTED_LIST_KEY)) {
            initialSelection = getIntent().getStringExtra(SELECTED_LIST_KEY);
        }

        // TODO store in savedInstanceState the selected list and available lists
        mSaveButton.setText("Save");
        mAvailableLists.add(NO_LIST_VALUE);
        mBookmarkList.setText(NO_LIST_VALUE);
        mUrlValidator = new BookmarkFormUtils.URLTextValidator(mSaveButton);
        mBookmarkUrl.addTextChangedListener(mUrlValidator);
        retrieveAvailableLists(initialSelection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBookmarkUrl.removeTextChangedListener(mUrlValidator);
        mRealm.close();
    }

    private void showLoading() {
        mSaveLoading.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.INVISIBLE);
    }

    private void showContent() {
        mSaveLoading.setVisibility(View.INVISIBLE);
        mContent.setVisibility(View.VISIBLE);
    }

    private void retrieveAvailableLists(final String initialSelection) {
        RealmUtils.retrieveListNames(mRealm, new Callbacks.OperationCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                mAvailableLists.addAll(result);
                Collections.sort(mAvailableLists);
                // if there is an initial selection and it is valid, set in list field
                if (!TextUtils.isEmpty(initialSelection) &&
                        mAvailableLists.contains(initialSelection)) {
                    mBookmarkList.setText(initialSelection);
                }
            }
            @Override
            public void onError(String msg, Throwable e) {
                Log.e(TAG, msg, e);
            }
        });
    }

    @OnClick(R.id.ib_bookmark_form_add_list)
    public void onAddListClick() {
        BookmarkFormUtils.onAddListClick(this, mAvailableLists, mBookmarkList);
    }

    @OnClick(R.id.button_bookmark_form_list)
    public void onChangeSelectedListClick() {
        BookmarkFormUtils.onChangeSelectedListClick(this, mAvailableLists, mBookmarkList);
    }

    @OnClick(R.id.button_bookmark_form_action)
    public void onSaveClick() {
        showLoading();

        // get fields from form and set in a unmanaged bookmark object
        String title = mBookmarkTitle.getText().toString();
        String url = mBookmarkUrl.getText().toString();
        String notes = mBookmarkNotes.getText().toString();
        String list = mBookmarkList.getText().toString();
        if (list.equals(NO_LIST_VALUE)) list = "";
        final Bookmark bookmark = new Bookmark();
        bookmark.setTitle(title);
        bookmark.setUrl(url);
        bookmark.setNotes(notes);
        bookmark.setListName(list);

        // check that url is ok
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "URL is empty when save button was clicked.");
        } else {
            TasksUtils.createBookmark(mRealm,
                    bookmark,
                    new Callbacks.OperationCallback<Void>() {
                        @Override
                        public void onSuccess(Void v) {
                            finish();
                        }
                        @Override
                        public void onError(String error, Throwable th) {
                            String msg = "There was an error creating a bookmark: " + bookmark + ". " + error;
                            Log.e(TAG, msg, th);
                            showContent();
                            Toast.makeText(NewBookmarkActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
