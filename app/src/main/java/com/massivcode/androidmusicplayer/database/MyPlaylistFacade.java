package com.massivcode.androidmusicplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by Ray Choe on 2015-11-27.
 */
public class MyPlaylistFacade {
    private static final String TAG = MyPlaylistFacade.class.getSimpleName();
    private DbHelper mHelper;
    private Context mContext;

    public static String[] projection
            = new String[]{MyPlaylistContract.MyPlaylistEntry._ID,
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST,
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID};

    public static String selection_music_id = MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID + "=?";
    public static String selection_playlist_name = MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST + "=?";

    public static String selection_toggle_favorite = MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST + " = ? and " +
            MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID + " = ? ";


    public MyPlaylistFacade(Context context) {
        mHelper = DbHelper.getInstance(context);
        mContext = context;
    }

    public void createDb() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        mHelper.onCreate(db);
        db.close();
    }

//    MediaStore.Audio.Media.ARTIST + " != ? and " + MediaStore.Audio.Media.ARTIST + " != ? " , new String[]{MediaStore.UNKNOWN_STRING, "김경호"}

    /**
     * 있으면 제거, 없으면 추가
     *
     * @param musicId
     */
    public void toggleFavoriteList(long musicId) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        Cursor cursor = db.query(MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, projection, selection_toggle_favorite, new String[]{MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_FAVORITE, String.valueOf(musicId)}, null, null, null);

        // 1. 기존에 이런 데이터가 없을 때 -> Insert
        if (cursor == null && cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_FAVORITE, MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_FAVORITE);
            values.put(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID, musicId);
            db.insert(MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, null, values);
            Log.d(TAG, "즐겨찾기에 " + musicId + " 를 추가하였습니다.");
        }
        // 2. 기존에 이런 데이터가 있을 때 -> Delete
        else {
            db.delete(MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, selection_toggle_favorite, new String[]{MyPlaylistContract.PlaylistNameEntry.PLAYLIST_NAME_FAVORITE, String.valueOf(musicId)});
            Log.d(TAG, "즐겨찾기에서 " + musicId + " 를 제거하였습니다.");
        }

        cursor.close();
        db.close();
    }

    /**
     * 사용자 정의 플레이 리스트
     * @param userPlaylistName
     * @param userPlayList
     */
    public void addUserPlaylist(String userPlaylistName, Long... userPlayList) {
        SQLiteDatabase db = null;
        SQLiteStatement statement;

        if(userPlayList != null && userPlayList.length != 0) {

            try {
                // 사용자가 추가하고자 하는 플레이리스트가 1곡일 때
                if (userPlayList.length == 1) {
                    db = mHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID, userPlayList[0]);
                    db.insert(MyPlaylistContract.MyPlaylistEntry.TABLE_NAME, null, values);
                    Log.d(TAG, "사용자 정의 플레이리스트에 1곡이 추가되었습니다.");
                    db.close();
                }

                // 사용자가 추가하고자 하는 플레이리스트가 1곡 이상일 때
                else {
                    db = mHelper.getWritableDatabase();
                    db.beginTransaction();

                    statement = db.compileStatement(
                            "INSERT INTO " + MyPlaylistContract.MyPlaylistEntry.TABLE_NAME + " ( " +
                                    MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST + " , " +
                                    MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID + " ) " +
                                    "values(?, ?)"

                    );

                    for (long id : userPlayList) {
                        int column = 1;
                        statement.bindString(column++, userPlaylistName);
                        statement.bindLong(column++, id);

                        statement.execute();
                    }

                    statement.close();
                    db.setTransactionSuccessful();

                }

            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                if(db != null) {
                    db.endTransaction();
                    db.close();
                }
            }

        }
    }

}
