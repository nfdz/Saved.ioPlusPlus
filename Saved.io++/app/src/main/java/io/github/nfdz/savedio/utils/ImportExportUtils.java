/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.nfdz.savedio.Callbacks;
import io.github.nfdz.savedio.R;
import io.github.nfdz.savedio.data.RealmUtils;
import io.github.nfdz.savedio.model.Bookmark;
import io.github.nfdz.savedio.model.serialization.BookmarkSerializer;
import io.github.nfdz.savedio.model.serialization.SerializationException;
import io.realm.Realm;
import timber.log.Timber;

public class ImportExportUtils {

    private static final int READ_REQUEST_CODE = 642;
    private static final int WRITE_REQUEST_CODE = 486;
    private static final String MIME_TYPE = "text/plain";
    private static final String SUGGESTED_NAME_FORMAT = "Bookmarks-%s.savediopp";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * This method starts open document system activity.
     * @param fm
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void importBookmarks(Fragment fm) {
        // choose a file via the system's file browser
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // show only results that can be "opened", such as a file
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // filter to show only plain text
        intent.setType(MIME_TYPE);

        fm.startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /**
     * This methods manage the result of an open document activity.
     * @param requestCode
     * @param resultCode
     * @param resultData
     * @param realm
     * @param context
     * @return true if activity result was managed by this method, false if not.
     */
    public static boolean onImportActivityResult(int requestCode,
                                                 int resultCode,
                                                 Intent resultData,
                                                 final Realm realm,
                                                 final Context context) {
        if (requestCode == READ_REQUEST_CODE) {
            // URI to user document is contained in the return intent
            if (resultCode == Activity.RESULT_OK && resultData != null && resultData.getData() != null) {
                final Uri uri = resultData.getData();
                new AsyncTask<Void, Void, Void>(){
                    private List<Bookmark> bookmarks;
                    private String error;
                    @Override
                    protected Void doInBackground(Void... params) {
                        InputStream in = null;
                        try {
                            DocumentFile file = DocumentFile.fromSingleUri(context, uri);
                            in = context.getContentResolver().openInputStream(file.getUri());
                            if (in.available() != 0) {
                                String inputStreamString = new Scanner(in).useDelimiter("\\A").next();
                                bookmarks = BookmarkSerializer.deserializeBookmarks(inputStreamString);;
                            } else {
                                error = context.getString(R.string.import_error_empty);
                            }
                        } catch (IOException e) {
                            error = context.getString(R.string.import_error_reading);
                        } catch (SerializationException e) {
                            error = context.getString(e.getMessageId());
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    // swallow
                                }
                            }
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void v) {
                        if (bookmarks != null) {
                            final int bookmarksToImport = bookmarks.size();
                            final AtomicInteger counter = new AtomicInteger(0);
                            final AtomicInteger successCounter = new AtomicInteger(0);
                            for (Bookmark bm : bookmarks) {
                                TasksUtils.createBookmark(context, realm, bm, new Callbacks.OperationCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        successCounter.incrementAndGet();
                                        finish();
                                    }
                                    @Override
                                    public void onError(String msg, Throwable th) {
                                        finish();
                                    }
                                    private void finish() {
                                        if (counter.incrementAndGet() == bookmarksToImport) {
                                            if (successCounter.get() == counter.get()) {
                                                Toast.makeText(context, R.string.import_success, Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(context, R.string.import_success_incomplete, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(context, context.getString(R.string.import_error_format, error), Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute();
            } else {
                Toast.makeText(context, R.string.file_error, Toast.LENGTH_LONG).show();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method starts create document system activity.
     * @param fm
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void exportBookmarks(Fragment fm) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // show only results that can be "opened", such as a file
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // create a file with plain text MIME type
        intent.setType(MIME_TYPE);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String currentDate = sdf.format(new Date());
        intent.putExtra(Intent.EXTRA_TITLE, String.format(SUGGESTED_NAME_FORMAT, currentDate));
        fm.startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    /**
     * This methods manage the result of an create document activity.
     * @param requestCode
     * @param resultCode
     * @param resultData
     * @param realm
     * @param context
     * @return true if activity result was managed by this method, false if not.
     */
    public static boolean onExportActivityResult(int requestCode,
                                                 final int resultCode,
                                                 Intent resultData,
                                                 Realm realm,
                                                 final Context context) {
        if (requestCode == WRITE_REQUEST_CODE) {
            // URI to user document is contained in the return intent
            if (resultCode == Activity.RESULT_OK && resultData != null && resultData.getData() != null) {
                final Uri uri = resultData.getData();
                RealmUtils.getSerializedBookmarks(realm, context, new Callbacks.OperationCallback<String>() {
                    @Override
                    public void onSuccess(final String serialized) {
                        // store it in the file
                        new AsyncTask<Void, Void, Void>(){
                            private boolean result = false;
                            @Override
                            protected Void doInBackground(Void... params) {
                                OutputStream out = null;
                                try {
                                    DocumentFile newFile = DocumentFile.fromSingleUri(context, uri);
                                    out = context.getContentResolver().openOutputStream(newFile.getUri());
                                    out.write(serialized.getBytes());
                                    result = true;
                                } catch (IOException e) {
                                    Timber.d(e, "There was an error writing file where to export bookmarks. ");
                                } finally {
                                    if (out != null) {
                                        try {
                                            out.close();
                                        } catch (IOException e) {
                                            // swallow
                                        }
                                    }
                                }
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void v) {
                                if (result) {
                                    Toast.makeText(context, R.string.export_success, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, R.string.export_error_writing, Toast.LENGTH_LONG).show();
                                }
                            }
                        }.execute();
                    }
                    @Override
                    public void onError(String msg, Throwable th) {
                        Toast.makeText(context, context.getString(R.string.export_error_format, msg), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(context, R.string.file_error, Toast.LENGTH_LONG).show();
            }
            return true;
        } else {
            return false;
        }
    }

}
