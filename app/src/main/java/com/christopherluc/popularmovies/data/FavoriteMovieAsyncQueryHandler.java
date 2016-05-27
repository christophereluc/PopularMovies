package com.christopherluc.popularmovies.data;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.lang.ref.SoftReference;

/**
 *
 * AsyncQueryHandler used to retrieve data from the content provider
 *
 * Created by Chris on 5/25/2016.
 */
public class FavoriteMovieAsyncQueryHandler extends AsyncQueryHandler {

    private SoftReference<QueryCallback> mCallback;

    public FavoriteMovieAsyncQueryHandler(ContentResolver cr, QueryCallback callback) {
        super(cr);
        mCallback = new SoftReference<>(callback);
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);
        QueryCallback callback = mCallback != null ? mCallback.get() : null;
        if (callback != null) {
            callback.onDeleteComplete(result > 0);
        }
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        QueryCallback callback = mCallback != null ? mCallback.get() : null;
        if (callback != null) {
            callback.onQueryComplete(cursor);
        }
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);
        QueryCallback callback = mCallback != null ? mCallback.get() : null;
        if (callback != null) {
            callback.onInsertComplete(uri != null);
        }
    }

    public interface QueryCallback {
        void onInsertComplete(boolean successful);

        void onDeleteComplete(boolean successful);

        void onQueryComplete(Cursor cursor);
    }
}
