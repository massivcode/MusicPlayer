package com.massivcode.androidmusicplayer.database;

import android.provider.BaseColumns;

/**
 * Created by Ray Choe on 2015-11-27.
 */
public class MyPlaylistContract {

    public MyPlaylistContract() {
    }

    public static abstract class MyPlaylistEntry implements BaseColumns {
        public static final String TABLE_NAME = "MyPlaylist";
        public static final String COLUMN_NAME_PLAYLIST = "playlist_name";
        public static final String COLUMN_NAME_MUSIC_ID = "music_id";
        public static final String COLUMN_NAME_LAST_PLAYED_TIME = "last_played_time";
        public static final String COLUMN_NAME_PLAY_COUNT = "play_count";
    }

    public static abstract class PlaylistNameEntry {
        public static final String PLAYLIST_NAME_MOST_PLAYED = "most_played";
        public static final String PLAYLIST_NAME_RECENTLY_PLAYED = "recently_played";
        public static final String PLAYLIST_NAME_FAVORITE = "favorite";
    }

}
