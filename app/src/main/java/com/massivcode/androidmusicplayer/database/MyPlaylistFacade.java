package com.massivcode.androidmusicplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Ray Choe on 2015-11-27.
 */
public class MyPlaylistFacade {
    private DbHelper mHelper;
    private Context mContext;

    public MyPlaylistFacade(Context context) {
        mHelper = DbHelper.getInstance(context);
        mContext = context;
    }

    public void createDb() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        mHelper.onCreate(db);
    }

}
