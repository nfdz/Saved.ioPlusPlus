/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio;

import android.app.SearchManager;
import android.content.Intent;
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
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.savedio.model.Bookmark;
import io.github.nfdz.savedio.model.BookmarkList;
import io.github.nfdz.savedio.utils.TasksUtils;
import io.github.nfdz.savedio.utils.ToolbarUtils;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Main activity of application. It shows a list of interactive bookmarks and has a lists navigation
 * menu at left.
 */
public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener,
        BookmarksAdapter.BookmarkOnClickHandler,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = MainActivity.class.getName();

    /** Key of the selected list in saved instance state */
    private static final String LIST_KEY = "selected-list";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.toolbar_logo) ImageView mLogo;
    @BindView(R.id.tv_main_error_message) TextView mErrorMessage;
    @BindView(R.id.swipe_refresh_main) SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.layout_main_content) LinearLayout mContent;
    @BindView(R.id.layout_main_list_info) LinearLayout mListInfo;
    @BindView(R.id.tv_main_list_name) TextView mListName;
    @BindView(R.id.rv_bookmarks) RecyclerView mBookmarksView;
    @BindView(R.id.drawer_layout_main) DrawerLayout mDrawerLayout;
    @BindView(R.id.nv_main_menu_list) ListView mNavigationListView;

    private ActionBarDrawerToggle mToggleNav;
    private BookmarksAdapter mBookmarksAdapter;
    private String mSelectedList = "";
    private ListsAdapter mListsAdaper;
    private Realm mRealm;

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
        if (savedInstanceState != null) mSelectedList = savedInstanceState.getString(LIST_KEY);

        mToggleNav = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mToolbar,
                R.string.main_nav_drawer_open,
                R.string.main_nav_drawer_close);

        int orientation = OrientationHelper.VERTICAL;
        boolean reverseLayout = false;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, orientation, reverseLayout);
        mBookmarksView.setLayoutManager(layoutManager);
        mBookmarksView.setHasFixedSize(true);
        mBookmarksAdapter = new BookmarksAdapter(this, null, this);
        mBookmarksView.setAdapter(mBookmarksAdapter);

        TouchHelperCallback touchHelperCallback = new TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(mBookmarksView);

        mListsAdaper = new ListsAdapter(this, null);
        mNavigationListView.setAdapter(mListsAdaper);

        updateLists();
        updateListLayout();
        updateBookmarks();
    }

    private void updateLists() {
        RealmResults<BookmarkList> bookmarkLists = mRealm.where(BookmarkList.class).findAll();
        mListsAdaper.updateData(bookmarkLists);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LIST_KEY, mSelectedList);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        ToolbarUtils.centerLogo(mToolbar, mLogo);
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawerLayout.addDrawerListener(mToggleNav);
        mToggleNav.syncState();
        mNavigationListView.setOnItemClickListener(this);
        mSwipeRefresh.setOnRefreshListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDrawerLayout.removeDrawerListener(mToggleNav);
        mNavigationListView.setOnItemClickListener(null);
        mSwipeRefresh.setOnRefreshListener(null);
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
     * This methods hides or shows the list info layout section depending on the value of
     * selected list (that could be null or empty).
     */
    private void updateListLayout() {
        if (TextUtils.isEmpty(mSelectedList)) {
            mListInfo.setVisibility(View.GONE);
        } else {
            mListName.setText(mSelectedList);
            mListInfo.setVisibility(View.VISIBLE);
        }
    }

    private void showBookmarks() {
        mContent.setVisibility(View.VISIBLE);
        mErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        mContent.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    private void showNothing() {
        mContent.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.INVISIBLE);
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
            // TODO implements settings
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.nv_main_menu_all)
    public void onAllListClick() {
        mSelectedList = "";
        updateListLayout();
        updateBookmarks();
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    /**
     * This method is invoked by navigation lists when user clicks one list.
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mSelectedList = mListsAdaper.getItem(position).getListName();
        updateListLayout();
        updateBookmarks();
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void updateBookmarks() {
        showNothing();
        OrderedRealmCollection<Bookmark> bookmarks = getBookmarks();
        mBookmarksAdapter.updateData(bookmarks);
        if (bookmarks != null) {
            showBookmarks();
        } else {
            showError();
        }
    }

    private OrderedRealmCollection<Bookmark> getBookmarks() {
        OrderedRealmCollection<Bookmark> result;
        if (TextUtils.isEmpty(mSelectedList)) {
            result = mRealm.where(Bookmark.class).findAll();
        } else {
            result = mRealm.where(BookmarkList.class)
                    .equalTo(BookmarkList.FIELD_LIST_NAME, mSelectedList)
                    .findFirst()
                    .getBookmarks();
        }
        return result;
    }

    /**
     * This method is invoked by recycler view adapter when user clicks in a bookmark.
     * @param bookmark
     */
    @Override
    public void onClick(Bookmark bookmark) {
        String url = bookmark.getUrl();
        Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        final Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
        searchIntent.putExtra(SearchManager.QUERY, url);
        PackageManager pm = getPackageManager();

        if (openIntent.resolveActivity(pm) != null) {
            startActivity(openIntent);
        } else if (searchIntent.resolveActivity(pm) != null) {
            Snackbar.make(mContent,
                    "Unable to open URL",
                    Snackbar.LENGTH_LONG)
                    .setAction("Search", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(searchIntent);
                        }
                    })
                    .show();
        } else {
            Snackbar.make(mContent,
                    "Unable to open URL",
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * This method is invoked by recycler view adapter when user performs a long click in a bookmark.
     * @param bookmark
     */
    @Override
    public void onLongClick(Bookmark bookmark) {
        Intent editBookmarkIntent = new Intent(this, EditBookmarkActivity.class);
        editBookmarkIntent.putExtra(EditBookmarkActivity.BOOKMARK_ID_KEY, bookmark.getId());
        startActivity(editBookmarkIntent);
    }

    /**
     * This method invoked by swipe refresh layout when the user pulls down the content.
     */
    @Override
    public void onRefresh() {
        // TODO sync data from internet
        mSwipeRefresh.setRefreshing(false);

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
                new Callback<Bookmark>() {
                    @Override
                    public void onSuccess(final Bookmark removedBookmark) {
                        // if bookmark was removed, ensure that selected list is not empty
                        if (removedBookmark != null) {
                            if (!TextUtils.isEmpty(mSelectedList)) {
                                BookmarkList list = mRealm.where(BookmarkList.class)
                                        .equalTo(BookmarkList.FIELD_LIST_NAME, mSelectedList)
                                        .findFirst();
                                if (list == null) {
                                    mSelectedList = "";
                                    updateListLayout();
                                    updateBookmarks();
                                }
                            }
                            Snackbar.make(mContent,
                                    "Bookmark deleted successfully",
                                    Snackbar.LENGTH_LONG)
                                    .setAction("Undo", new View.OnClickListener() {
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
                new Callback<Void>() {
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
