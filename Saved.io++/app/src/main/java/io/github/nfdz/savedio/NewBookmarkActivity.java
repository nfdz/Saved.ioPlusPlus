/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import timber.log.Timber;

/**
 * This activity shows an empty bookmark layout form and manage its creation.
 */
public class NewBookmarkActivity extends AppCompatActivity {

    /** Key of the selected list in extra data intent map and in saved instance state */
    public static final String SELECTED_LIST_KEY = "selected-list";

    /** Valid mime type for extra data in intent */
    private static final String VALID_MIME_TYPE = "text/";

    private static final String NO_LIST_VALUE = "-";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.toolbar_logo) ImageView mLogo;
    @BindView(R.id.et_bookmark_form_title) EditText mBookmarkTitle;
    @BindView(R.id.et_bookmark_form_url) EditText mBookmarkUrl;
    @BindView(R.id.et_bookmark_form_notes) EditText mBookmarkNotes;
    @BindView(R.id.pb_new_bookmark_loading) ProgressBar mSaveLoading;
    @BindView(R.id.tv_bookmark_form_list) TextView mBookmarkList;
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

        // extract initial selection from saved state
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_LIST_KEY)) {
            initialSelection = savedInstanceState.getString(SELECTED_LIST_KEY);
        }

        // extract data from intent (add new bookmark share option)
        if (savedInstanceState == null &&
                getIntent() != null &&
                getIntent().getAction() != null &&
                getIntent().getType() != null &&
                getIntent().hasExtra(Intent.EXTRA_TEXT)) {
            String action = getIntent().getAction();
            String type = getIntent().getType();
            if (action.equals(Intent.ACTION_SEND) && type.startsWith(VALID_MIME_TYPE)) {
                String receivedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
                mBookmarkUrl.setText(receivedText);
            }
        }

        mSaveButton.setText(R.string.new_bookmark_button);
        mAvailableLists.add(NO_LIST_VALUE);
        mBookmarkList.setText(NO_LIST_VALUE);
        mUrlValidator = new BookmarkFormUtils.URLTextValidator(mSaveButton);
        mBookmarkUrl.addTextChangedListener(mUrlValidator);
        retrieveAvailableLists(initialSelection);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String selectedList = mBookmarkList.getText().toString();
        if (!TextUtils.isEmpty(selectedList)) {
            outState.putString(SELECTED_LIST_KEY, selectedList);
        }
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
                // if there is an initial selection and it is valid, set in list field
                if (!TextUtils.isEmpty(initialSelection)) {
                    if (!mAvailableLists.contains(initialSelection)) mAvailableLists.add(initialSelection);
                    mBookmarkList.setText(initialSelection);
                }
                Collections.sort(mAvailableLists);
            }
            @Override
            public void onError(String msg, Throwable e) {
                Timber.e(e, msg);
            }
        });
    }

    @OnClick(R.id.ib_bookmark_form_add_list)
    public void onAddListClick() {
        BookmarkFormUtils.onAddListClick(this, mAvailableLists, mBookmarkList);
    }

    @OnClick(R.id.tv_bookmark_form_list)
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
        if (TextUtils.isEmpty(title)) title = BookmarkFormUtils.inferTitleFromURL(url);
        final Bookmark bookmark = new Bookmark();
        bookmark.setTitle(title);
        bookmark.setUrl(url);
        bookmark.setNotes(notes);
        bookmark.setListName(list);

        // check that url is ok
        if (TextUtils.isEmpty(url)) {
            Timber.e("URL is empty when save button was clicked.");
        } else {
            TasksUtils.createBookmark(this,
                    mRealm,
                    bookmark,
                    new Callbacks.OperationCallback<Void>() {
                        @Override
                        public void onSuccess(Void v) {
                            finish();
                        }
                        @Override
                        public void onError(String error, Throwable th) {
                            String msg = "There was an error creating a bookmark: " + bookmark + ". " + error;
                            Timber.e(th, msg);
                            showContent();
                            Toast.makeText(NewBookmarkActivity.this,
                                    R.string.new_bookmark_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
