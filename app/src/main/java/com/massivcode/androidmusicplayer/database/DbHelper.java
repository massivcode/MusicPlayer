package com.massivcode.androidmusicplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ray Choe on 2015-11-27.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "EasyMusic.db";
    public static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_MY_PLAYLIST =
            "CREATE TABLE IF NOT EXISTS " + MyPlaylistContract.MyPlaylistEntry.TABLE_NAME + " (" +
                    MyPlaylistContract.MyPlaylistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST + " TEXT NOT NULL, " +
                    MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID + " INTEGER NOT NULL, " +
                    MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST_TYPE + " TEXT NOT NULL UNIQUE " +
                    ");";

    private static DbHelper sSingleton = null;

    public static synchronized  DbHelper getInstance(Context context) {
        if(sSingleton == null) {
            sSingleton = new DbHelper(context);
        }
        return sSingleton;
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MY_PLAYLIST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
