/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.savedio.data.PreferencesUtils;
import io.github.nfdz.savedio.data.RealmUtils;
import io.github.nfdz.savedio.model.Bookmark;
import io.github.nfdz.savedio.model.BookmarkDateComparator;
import io.github.nfdz.savedio.model.BookmarkList;
import io.github.nfdz.savedio.model.BookmarkTitleComparator;
import io.github.nfdz.savedio.sync.SyncUtils;
import io.github.nfdz.savedio.utils.TasksUtils;
import io.github.nfdz.savedio.utils.ToolbarUtils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Main activity of application. It shows a list of interactive bookmarks and has a lists navigation
 * menu at left.
 */
public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener,
        BookmarksAdapter.BookmarkOnClickHandler,
        SwipeRefreshLayout.OnRefreshListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getName();
    private static final int ALL_CONTENT = 0;
    private static final int FAVORITE_CONTENT = 1;
    private static final int LIST_CONTENT = 2;
    private static final String NO_LIST = "";

    /** Key of the selected list in saved instance state */
    private static final String LIST_KEY = "selected-list";

    /** Key of the selected content in saved instance state */
    private static final String CONTENT_KEY = "selected-content";

    /** Key of the bookmark recycler view position in saved instance state */
    private static final String LIST_POSITION_KEY = "bookmark-list-position";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.toolbar_logo) ImageView mLogo;
    @BindView(R.id.swipe_refresh_main) SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.layout_main_content) LinearLayout mContent;
    @BindView(R.id.layout_main_content_info) LinearLayout mContentInfo;
    @BindView(R.id.tv_main_content_name) TextView mContentName;
    @BindView(R.id.rv_bookmarks) RecyclerView mBookmarksView;
    @BindView(R.id.drawer_layout_main) DrawerLayout mDrawerLayout;
    @BindView(R.id.nv_main_menu_list) ListView mNavigationListView;
    @BindView(R.id.switch_main_content) Switch mContentSwitch;

    private ActionBarDrawerToggle mToggleNav;
    private BookmarksAdapter mBookmarksAdapter;
    private int mSelectedContent;
    private String mSelectedList;
    private ListsAdapter mListsAdaper;
    private Realm mRealm;
    private LinearLayoutManager mLayoutManager;
    private int mLastPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        ToolbarUtils.setUpActionBar(getSupportActionBar());
        Realm.init(this);

        mRealm = Realm.getDefaultInstance();
        if (savedInstanceState != null) {
            mSelectedContent = savedInstanceState.getInt(CONTENT_KEY, ALL_CONTENT);
            mSelectedList = savedInstanceState.getString(LIST_KEY, NO_LIST);
            mLastPosition = savedInstanceState.getInt(LIST_POSITION_KEY, RecyclerView.NO_POSITION);
        }

        mToggleNav = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mToolbar,
                R.string.main_nav_drawer_open,
                R.string.main_nav_drawer_close);

        int orientation = OrientationHelper.VERTICAL;
        boolean reverseLayout = false;
        mLayoutManager = new LinearLayoutManager(this, orientation, reverseLayout);
        mBookmarksView.setLayoutManager(mLayoutManager);
        mBookmarksView.setHasFixedSize(true);
        mBookmarksAdapter = new BookmarksAdapter(this, this);
        mBookmarksView.setAdapter(mBookmarksAdapter);

        TouchHelperCallback touchHelperCallback = new TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(mBookmarksView);

        mListsAdaper = new ListsAdapter(this, null);
        mNavigationListView.setAdapter(mListsAdaper);

        SyncUtils.initialize(this);
    }

    private void updateLists() {
        RealmResults<BookmarkList> bookmarkLists = mRealm.where(BookmarkList.class).findAll();
        mListsAdaper.updateData(bookmarkLists);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CONTENT_KEY, mSelectedContent);
        outState.putString(LIST_KEY, mSelectedList);
        outState.putInt(LIST_POSITION_KEY, mLayoutManager.findFirstCompletelyVisibleItemPosition());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // add listeners
        mDrawerLayout.addDrawerListener(mToggleNav);
        mToggleNav.syncState();
        mNavigationListView.setOnItemClickListener(this);
        mSwipeRefresh.setOnRefreshListener(this);
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // update adapter comparator with preferences
        PreferencesUtils.retrievePreferredSort(this, new Callbacks.FinishCallback<String>() {
            @Override
            public void onFinish(String sort) {
                if (getString(R.string.pref_sort_date_key).equals(sort)) {
                    mBookmarksAdapter.setComparator(new BookmarkDateComparator());
                } else {
                    mBookmarksAdapter.setComparator(new BookmarkTitleComparator());
                }
                updateLists();
                updateInfoLayout();
                List<Bookmark> bookmarks = updateBookmarks();
                // ensure that there are some bookmarks if a list selected
                if (mSelectedContent == LIST_CONTENT &&
                        (bookmarks == null || bookmarks.size() == 0)) {
                    mSelectedList = NO_LIST;
                    mSelectedContent = ALL_CONTENT;
                    updateInfoLayout();
                    updateBookmarks();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDrawerLayout.removeDrawerListener(mToggleNav);
        mNavigationListView.setOnItemClickListener(null);
        mSwipeRefresh.setOnRefreshListener(null);
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * This methods hides or shows the info layout section depending on the value of
     * selected content.
     */
    private void updateInfoLayout() {
        switch (mSelectedContent) {
            case LIST_CONTENT:
                mContentName.setText(mSelectedList);
                mContentSwitch.setVisibility(View.VISIBLE);
                mContentInfo.setVisibility(View.VISIBLE);
                break;
            case FAVORITE_CONTENT:
                mContentName.setText(getString(R.string.main_content_favorites));
                mContentSwitch.setVisibility(View.GONE);
                mContentInfo.setVisibility(View.VISIBLE);
                break;
            default:
                mContentInfo.setVisibility(View.GONE);
        }
    }

    private void showBookmarks() {
        mContent.setVisibility(View.VISIBLE);
    }

    private void showNothing() {
        mContent.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.fab_add_bookmark)
    public void handleAddBookmark(View view) {
        Intent newBookmarkIntent = new Intent(this, NewBookmarkActivity.class);
        if (!TextUtils.isEmpty(mSelectedList)) {
            newBookmarkIntent.putExtra(NewBookmarkActivity.SELECTED_LIST_KEY, mSelectedList);
        }
        startActivity(newBookmarkIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.nv_main_menu_all)
    public void onAllListClick() {
        mSelectedList = NO_LIST;
        mSelectedContent = ALL_CONTENT;
        updateInfoLayout();
        updateBookmarks();
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @OnClick(R.id.nv_main_menu_favorites)
    public void onFavoriteListClick() {
        mSelectedList = NO_LIST;
        mSelectedContent = FAVORITE_CONTENT;
        updateInfoLayout();
        updateBookmarks();
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    /**
     * This method is invoked by navigation lists when user clicks one list.
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mSelectedContent = LIST_CONTENT;
        mSelectedList = mListsAdaper.getItem(position).getListName();
        updateInfoLayout();
        updateBookmarks();
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    /**
     * Update the content of recycler view performing a new query in realm.
     * @return the new data used by adapter, it could be null.
     */
    private RealmResults<Bookmark> updateBookmarks() {
        showNothing();
        RealmResults<Bookmark> bookmarks = getBookmarks();
        mBookmarksAdapter.swapData(bookmarks);
        if (mLastPosition != RecyclerView.NO_POSITION) {
            mBookmarksView.scrollToPosition(mLastPosition);
            mLastPosition = RecyclerView.NO_POSITION;
        }
        showBookmarks();
        return bookmarks;
    }

    /**
     * Perform a query in realm depending of the selected content flag.
     * @return results of the query, it could be null.
     */
    private RealmResults<Bookmark> getBookmarks() {
        RealmResults<Bookmark> result;
        switch (mSelectedContent) {
            case FAVORITE_CONTENT:
                result = mRealm.where(Bookmark.class)
                        .equalTo(Bookmark.FIELD_FAVORITE, true)
                        .findAll();
                break;
            case LIST_CONTENT:
                BookmarkList list = mRealm.where(BookmarkList.class)
                        .equalTo(BookmarkList.FIELD_LIST_NAME, mSelectedList)
                        .findFirst();
                if (list != null) {
                    result = list.getBookmarks()
                            .where()
                            .findAll();
                } else {
                    result = null;
                }
                break;
            default:
                result = mRealm.where(Bookmark.class).findAll();
        }
        return result;
    }

    /**
     * This method is invoked by recycler view adapter when user clicks in a bookmark.
     * @param bookmark
     */
    @Override
    public void onBookmarkClick(Bookmark bookmark) {
        String url = bookmark.getUrl();
        Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        final Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
        searchIntent.putExtra(SearchManager.QUERY, url);
        PackageManager pm = getPackageManager();

        if (openIntent.resolveActivity(pm) != null) {
            startActivity(openIntent);
        } else if (searchIntent.resolveActivity(pm) != null) {
            Snackbar.make(mContent,
                    getString(R.string.main_bookmark_unable_click),
                    Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.main_bookmark_unable_click_search), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(searchIntent);
                        }
                    })
                    .show();
        } else {
            Snackbar.make(mContent,
                    getString(R.string.main_bookmark_unable_click),
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * This method is invoked by recycler view adapter when user performs a long click in a bookmark.
     * @param bookmark
     */
    @Override
    public void onLongBookmarkClick(Bookmark bookmark) {
        Intent editBookmarkIntent = new Intent(this, EditBookmarkActivity.class);
        editBookmarkIntent.putExtra(EditBookmarkActivity.BOOKMARK_ID_KEY, bookmark.getId());
        startActivity(editBookmarkIntent);
    }

    @Override
    public void onFavoriteClick(Bookmark bookmark) {
        // toggle favorite flag
        boolean isFavorite = !bookmark.isFavorite();
        RealmUtils.setFavorite(mRealm, bookmark.getId(), isFavorite, new Callbacks.OperationCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // nothing to do
            }
            @Override
            public void onError(String msg, Throwable th) {
                Log.e(TAG, msg, th);
            }
        });
    }

    /**
     * This method invoked by swipe refresh layout when the user pulls down the content.
     */
    @Override
    public void onRefresh() {

        SyncUtils.startImmediateSync(this);

        // just testing the API

//        APIHelper helper = new APIHelper();
//
//
//        Call<List<BookmarkAPI>> call = helper.getAPI().retrieveAllBookmarks(BuildConfig.SAVEDIO_API_DEV_KEY,
//                "",
//                null, null, null);
//
//        call.enqueue(new Callback<List<BookmarkAPI>>() {
//            @Override
//            public void onResponse(Call<List<BookmarkAPI>> call, Response<List<BookmarkAPI>> response) {
//                for (BookmarkAPI bm : response.body()) {
//                    System.out.println("bm: "+bm.id);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<BookmarkAPI>> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });
//
//        Call<BookmarkAPI> call = helper.getAPI().retrieveSingleBookmark(
//                "",
//                BuildConfig.SAVEDIO_API_DEV_KEY,
//                "");
//
//        call.enqueue(new Callback<BookmarkAPI>() {
//            @Override
//            public void onResponse(Call<BookmarkAPI> call, Response<BookmarkAPI> response) {
//                System.out.println("bm: "+response.body().id);
//            }
//
//            @Override
//            public void onFailure(Call<BookmarkAPI> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });
//
//
//        Call<CreateBookmarkResponse> call = helper.getAPI().createBookmark(BuildConfig.SAVEDIO_API_DEV_KEY,
//                "",
//                "https://saved.io/", "title", null);
//
//        call.enqueue(new Callback<CreateBookmarkResponse>() {
//            @Override
//            public void onResponse(Call<CreateBookmarkResponse> call, Response<CreateBookmarkResponse> response) {
//                System.out.println("\nCreated bookmark: " + response.body().id + "/" +
//                        response.body().url + "/" + response.body().title);
//            }
//
//            @Override
//            public void onFailure(Call<CreateBookmarkResponse> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });
//
//
//        Call<Void> call = helper.getAPI().deleteBookmark(BuildConfig.SAVEDIO_API_DEV_KEY,
//                "",
//                "");
//
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                System.out.println("\nDeleted bookmark: " + response.isSuccessful());
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });

        // TODO sync data from internet
        mSwipeRefresh.setRefreshing(false);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_key))) {
            PreferencesUtils.retrievePreferredSort(this, new Callbacks.FinishCallback<String>() {
                @Override
                public void onFinish(String sort) {
                    if (getString(R.string.pref_sort_date_key).equals(sort)) {
                        mBookmarksAdapter.setComparator(new BookmarkDateComparator());
                    } else {
                        mBookmarksAdapter.setComparator(new BookmarkTitleComparator());
                    }
                }
            });
        }
    }

    /**
     * Bookmark touch helper callback implementation.
     */
    private class TouchHelperCallback extends ItemTouchHelper.SimpleCallback {
        TouchHelperCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        /**
         * This method is invoked when a user swipe a bookmark.
         * @param viewHolder
         * @param direction
         */
        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            String bookmarkId = (String) viewHolder.itemView.getTag();
            deleteBookmark(bookmarkId);
        }
    }

    private void deleteBookmark(final String bookmarkId) {
        TasksUtils.deleteBookmark(mRealm,
                bookmarkId,
                new Callbacks.OperationCallback<Bookmark>() {
                    @Override
                    public void onSuccess(final Bookmark removedBookmark) {
                        // if bookmark was removed, ensure that selected list is not empty
                        if (removedBookmark != null) {
                            if (mSelectedContent == LIST_CONTENT &&
                                    !TextUtils.isEmpty(mSelectedList)) {
                                if (mBookmarksAdapter.getItemCount() == 0) {
                                    mSelectedContent = ALL_CONTENT;
                                    mSelectedList = NO_LIST;
                                    updateInfoLayout();
                                    updateBookmarks();
                                }
                            }
                            Snackbar.make(mContent,
                                    getString(R.string.main_bookmark_deleted),
                                    Snackbar.LENGTH_LONG)
                                    .setAction(getString(R.string.main_bookmark_deleted_undo), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            insertBookmark(removedBookmark);
                                        }
                                    }).show();
                        } else {
                            onError("There is no bookmarks with that ID. ", new Throwable());
                        }
                    }
                    @Override
                    public void onError(String msg, Throwable th) {
                        Log.e(TAG, "There was an error deleting a bookmark: " + bookmarkId+". " + msg, th);
                        mBookmarksAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void insertBookmark(final Bookmark bookmark) {
        TasksUtils.createBookmark(mRealm,
                bookmark,
                new Callbacks.OperationCallback<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        // nothing
                    }
                    @Override
                    public void onError(String msg, Throwable th) {
                        Log.e(TAG, "There was an error inserting a bookmark: " + bookmark + ". " + msg, th);
                    }
                });
    }
}
