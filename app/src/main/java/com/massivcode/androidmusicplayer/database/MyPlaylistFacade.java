package com.massivcode.androidmusicplayer.database;

import android.content.Context;

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

}
