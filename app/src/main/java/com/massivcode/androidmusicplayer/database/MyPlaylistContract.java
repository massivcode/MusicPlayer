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
        public static final String COLUMN_NAME_PLAYLIST_TYPE = "playlist_type";
    }

    public static abstract class PlaylistNameEntry {
        // 사용자가 플레이어의 별 아이콘을 눌러 추가한 목록 == 즐겨찾기
        public static final String PLAYLIST_NAME_FAVORITE = "favorite";
        // 바로 이전에 앱을 종료할 당시의 플레이리스트
        public static final String PLAYLIST_NAME_LAST_PLAYED = "last_played";
        // 사용자 정의 플레이리스트
        public static final String PLAYLIST_NAME_USER_DEFINITION = "user_definition";
    }

}
