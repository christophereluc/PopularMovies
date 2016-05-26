package com.christopherluc.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Chris on 5/25/2016.
 */
public class FavoriteMovieDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "favorite_movie.db";
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public FavoriteMovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_FAVORITE_MOVIE_TABLE = "CREATE TABLE " + FavoriteMovieContract.FavoriteMovieEntry.TABLE_NAME + " (" +
                FavoriteMovieContract.FavoriteMovieEntry._ID + " INTEGER PRIMARY KEY," +
                FavoriteMovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                FavoriteMovieContract.FavoriteMovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                FavoriteMovieContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                FavoriteMovieContract.FavoriteMovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                FavoriteMovieContract.FavoriteMovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                " );";


        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteMovieContract.FavoriteMovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
