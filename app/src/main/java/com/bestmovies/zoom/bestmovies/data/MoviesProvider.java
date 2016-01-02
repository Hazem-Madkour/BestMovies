package com.bestmovies.zoom.bestmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by ZooM- on 12/27/2015.
 */
public class MoviesProvider extends ContentProvider {

    private MoviesDBHelper mMoviesDBHelper;

    @Override
    public boolean onCreate() {
        mMoviesDBHelper = new MoviesDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        retCursor = mMoviesDBHelper.getReadableDatabase().query(
                MoviesContract.MoviesColumns.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mMoviesDBHelper.getWritableDatabase();
        Uri returnUri;
        long _id = db.insert(MoviesContract.MoviesColumns.TABLE_NAME, null, contentValues);
        if ( _id > 0 )
            returnUri = MoviesContract.MoviesColumns.getMoviesUri(_id);
        else
            throw new android.database.SQLException("Failed to insert row into " + uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMoviesDBHelper.getWritableDatabase();
        int rowsDeleted;
        rowsDeleted = db.delete(
                MoviesContract.MoviesColumns.TABLE_NAME, selection, selectionArgs);
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMoviesDBHelper.getWritableDatabase();
        int rowsUpdated = db.update(MoviesContract.MoviesColumns.TABLE_NAME, contentValues, selection,
                selectionArgs);
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mMoviesDBHelper.close();
        super.shutdown();
    }
}
