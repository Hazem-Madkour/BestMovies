package com.bestmovies.zoom.bestmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.bestmovies.zoom.bestmovies.data.MoviesContract.MoviesColumns;

/**
 * Created by ZooM- on 12/27/2015.
 */
public class MoviesDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movies.db";

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesColumns.TABLE_NAME + " (" +
                MoviesColumns._ID + " INTEGER PRIMARY KEY," +
                MoviesColumns.COLUMN_KEY + " TEXT UNIQUE NOT NULL, " +
                MoviesColumns.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesColumns.COLUMN_RELEASE_DATE + " TEXT , " +
                MoviesColumns.COLUMN_MOVIE_POSTER + " TEXT , " +
                MoviesColumns.COLUMN_VOTE_AVERAGE + " REAL NOT NULL ," +
                MoviesColumns.COLUMN_OVERVIEW + " TEXT NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }
}
