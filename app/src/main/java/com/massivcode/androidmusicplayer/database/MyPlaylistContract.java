/*
 * Copyright 2015. Pureum Choe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.massivcode.androidmusicplayer.database;

import android.provider.BaseColumns;


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
