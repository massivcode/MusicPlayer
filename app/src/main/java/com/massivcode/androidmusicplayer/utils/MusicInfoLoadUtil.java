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

package com.massivcode.androidmusicplayer.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.database.MyPlaylistContract;
import com.massivcode.androidmusicplayer.database.MyPlaylistFacade;
import com.massivcode.androidmusicplayer.models.MusicInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class MusicInfoLoadUtil {

    private static final String TAG = MusicInfoLoadUtil.class.getSimpleName();

    /**
     * 음원 ID, 음원 제목, 음원 가수, 음원 앨범, 음원 길이
     */
    public static String[] projection = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION};

    public static String selection = "_id=?";
    public static String selection_artist = MediaStore.Audio.Media.ARTIST + "=?";

//    MediaStore.Audio.Media.ARTIST + " != ? and " + MediaStore.Audio.Media.ARTIST + " != ? " , new String[]{MediaStore.UNKNOWN_STRING, "김경호"}

    /**
     * 모든 음원 중에서 <unknown>을 제외하고 Artist 또는 Title 이 KeyWord 와 동일한 것을 검색
     *
     * @param context
     * @param keyWord
     * @return
     */
    public static Cursor search(Context context, String keyWord) {
        // 모든 음원 중에서 <unknown>을 제외하고 Artist 또는 Title 이 KeyWord 와 동일한 것을 검색
//        String where = MediaStore.Audio.Media.ARTIST + " != ? " + " AND "
//          + MediaStore.Audio.Media.ARTIST + " like '%?%' OR "
//          + MediaStore.Audio.Media.TITLE + " like '%" + "?" + "%'";
//        new String[]{MediaStore.UNKNOWN_STRING, keyWord, keyWord}
//        살려주세여
        String where = MediaStore.Audio.Media.ARTIST + " != ? " + " AND " + MediaStore.Audio.Media.ARTIST + " like '%" + keyWord + "%' OR " + MediaStore.Audio.Media.TITLE + " like '%" + keyWord + "%'";
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, where, new String[]{MediaStore.UNKNOWN_STRING}, null);
    }

    /**
     * 앱이 실행하자마자 뮤직 서비스에서 실행하는 것으로, 기기내 모든 음원 정보를 담고 있는 맵을 리턴한다.
     *
     * @param context
     * @return
     */
    public static HashMap<Long, MusicInfo> getAllMusicInfo(Context context) {
        HashMap<Long, MusicInfo> map = new HashMap<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.ARTIST + " != ? AND " + MediaStore.Audio.Media.TITLE + " NOT LIKE '%" + "hangout" + "%'" , new String[]{MediaStore.UNKNOWN_STRING}, null);


        if (cursor != null || cursor.getCount() != 0)
            while (cursor.moveToNext()) {

                long _id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                Uri uri = Uri.parse("content://media/external/audio/media/" + _id);
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                if (duration != null) {
                    MusicInfo musicInfo = new MusicInfo(_id, uri, artist, title, album, Integer.parseInt(duration));
                    map.put(_id, musicInfo);
                }

            }


        return map;
    }

    /**
     * playlistFragment 의 ListView 에서 자식 아이템을 클릭하면 해당 플레이리스트의 모든 뮤직 아이디를 리스트에 담아 리턴함.
     *
     * @param playlistName
     * @return
     */
    public static ArrayList<Long> getMusicIdListFromPlaylistName(String playlistName, Context context) {
        ArrayList<Long> list = new ArrayList<>();

        MyPlaylistFacade facade = new MyPlaylistFacade(context);
        Cursor cursor = facade.getSelectedPlaylistMusicIds(playlistName);

        while (cursor.moveToNext()) {
            long id = cursor.getInt(cursor.getColumnIndexOrThrow(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID));
            list.add(id);
        }

        cursor.close();
        return list;
    }

    /**
     * 현재 재생목록 팝업에서 사용하는 것으로, 뮤직 서비스에서 가지고 있는 기기내 모든 음원 정보 중,
     * 현재 플레이 중인 음원 리스트만을 리턴하는 것.
     *
     * @param origin
     * @param keys
     * @return
     */
    public static ArrayList<MusicInfo> switchAllMusicInfoToSelectedMusicInfo(HashMap<Long, MusicInfo> origin, ArrayList<Long> keys) {
        ArrayList<MusicInfo> list = new ArrayList<>();

        Log.d(TAG, "allMusicInfo.size : " + origin.size());
        Log.d(TAG, "keys.size : " + keys.size());


        for (int i = 0; i < keys.size(); i++) {

            long key;

            if (keys.size() > 1) {
                key = keys.get(i);
                if (origin.get(key) != null) {
                    list.add(origin.get(key));
                }
            } else {
                key = keys.get(0);
                if (origin.get(key) != null) {
                    list.add(origin.get(key));
                    break;
                }
            }


        }

        return list;
    }

    public static String[] getArtistAndTitleFromId(Context context, int id) {
        String[] result = new String[2];

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media._ID + " = ?", new String[]{String.valueOf(id)}, null);
        if (cursor != null || cursor.getCount() != 0) {
            cursor.moveToFirst();
            result[0] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            result[1] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            cursor.close();
        }
        return result;
    }

    public static ArrayList<MusicInfo> getMusicInfoByIds(Context context, ArrayList<Long> ids) {
        ArrayList<MusicInfo> list = new ArrayList<>();

        for (Long id : ids) {
            MusicInfo musicInfo = new MusicInfo();
            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media._ID + " = ?", new String[]{String.valueOf(id)}, null);
            if (cursor != null || cursor.getCount() != 0) {

                while (cursor.moveToNext()) {
                    long _id = id;
                    Uri uri = Uri.parse("content://media/external/audio/media/" + _id);
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                    musicInfo.set_id(_id);
                    musicInfo.setUri(uri);
                    musicInfo.setArtist(artist);
                    musicInfo.setTitle(title);
                    musicInfo.setAlbum(album);
                    if(duration != null) {
                        musicInfo.setDuration(Integer.parseInt(duration));
                    }
                    list.add(musicInfo);
                }
            }
        }

        return list;
    }

    public static ArrayList<Long> getIdListByMusicInfoList(ArrayList<MusicInfo> origin) {
        ArrayList<Long> list = new ArrayList<>();

        for (int i = 0; i < origin.size(); i++) {
            MusicInfo musicInfo = origin.get(i);
            list.add(musicInfo.get_id());
        }

        return list;

    }


    /**
     * 서비스에서 해당 곡의 정보를 얻을 때 사용한다.
     * 곡을 재생할 때, 다음/이전 곡으로 넘길 때 마다 사용된다.
     *
     * @param context
     * @param id
     * @return
     */
    public static MusicInfo getSelectedMusicInfo(Context context, long id) {
        MusicInfo musicInfo = null;
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, new String[]{String.valueOf(id)}, null);
        if (cursor != null || cursor.getCount() != 0) {

            cursor.moveToFirst();
            long _id = id;
            Uri uri = Uri.parse("content://media/external/audio/media/" + _id);
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, uri);

            byte[] albumArt = retriever.getEmbeddedPicture();

            if(duration != null) {
                musicInfo = new MusicInfo(_id, uri, artist, title, album, albumArt, Integer.parseInt(duration));
            }

            cursor.close();
        }


        return musicInfo;


    }

    /**
     * ArtistFragment에서 사용하는 것으로 기기내 모든 아티스트 정보를 얻어 Cursor로 리턴하는 것이다.
     *
     * @param context
     * @return
     */
    public static Cursor getArtistInfo(Context context) {
        String[] projection = new String[]{
                MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        };

        return context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.ARTIST + " != ?", new String[]{MediaStore.UNKNOWN_STRING}, null);
    }

    /**
     * ArtistFragment에서 사용하는 것으로 입력된 아티스트의 모든 음원 정보를 Cursor로 리턴한다.
     *
     * @param context
     * @param artist
     * @return
     */
    public static Cursor getArtistTrackInfoCursor(Context context, String artist) {
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection_artist, new String[]{artist}, null);
    }

    /**
     * 해당 아티스트의 모든 음원 정보를 리턴한다.
     *
     * @param context
     * @param artist
     * @return
     */
    public static ArrayList<Long> getArtistTrackInfoList(Context context, String artist) {
        ArrayList<Long> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection_artist, new String[]{artist}, null);

        if (cursor != null || cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                list.add(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
            }

            cursor.close();
        }

        return list;
    }


    /**
     * 앱 종료 후에 다시 실행했을 때, 최종 정보가 담긴 플레이 리스트를 리턴한다.
     *
     * @param values
     * @return
     */
    public static ArrayList<Long> getLastPlayedSongs(Set<String> values) {
        ArrayList<Long> list = new ArrayList<>();
        for (String value : values) {
            list.add(Long.getLong(value));
        }
        return list;
    }

    /**
     * 앱 종료시 SharedPreference 에 StringSet을 저장해야하는데, 현재 플레이 리스트를 받아서 이것을 set으로 저장한다.
     *
     * @param values
     * @return
     */
    public static Set<String> getPlaylistToSet(ArrayList<Long> values) {
        Set<String> set = new HashSet<>();

        for (Long value : values) {
            set.add(String.valueOf(value));
        }

        return set;
    }

    /**
     * 단일 재생 시, SongsFragment의 ListView에서 선택한 아이템으로부터 _id 정보를 얻어와 그것을 list에 담아 리턴한다.
     *
     * @param context
     * @param cursor
     * @return
     */
    public static ArrayList<Long> getSelectedSongPlaylist(Context context, Cursor cursor) {
        ArrayList<Long> list = new ArrayList<>();
        list.add(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
        return list;
    }

    /**
     * 아티스트 리스트뷰의 차일드 아이템을 클릭하면 단일 재생을 하는데, 그때 이것을 이용한다.
     *
     * @param id
     * @return
     */
    public static ArrayList<Long> getSelectedSongPlaylist(long id) {
        ArrayList<Long> list = new ArrayList<>();
        list.add(id);
        return list;
    }

    /**
     * 모두 재생 시, 기기 내에 존재하는 모든 로컬 파일들로부터 _id 정보를 얻어와 그것을 list에 담아 리턴한다.
     *
     * @param context
     * @return
     */
    public static ArrayList<Long> getPlayAllList(Context context) {
        ArrayList<Long> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.ARTIST + " != ? AND " + MediaStore.Audio.Media.TITLE + " NOT LIKE '%" + "hangout" + "%'", new String[]{MediaStore.UNKNOWN_STRING}, null);

        if(cursor != null || cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                list.add(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
            }

            cursor.close();
        }

        return list;
    }


    public static Bitmap getBitmap(Context context, Uri uri, int quality) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);

        byte[] albumArt = retriever.getEmbeddedPicture();

        // Bitmap 샘플링
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = quality; // 2의 배수

        Bitmap bitmap;
        if (null != albumArt) {
            bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length, options);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_no_image);
        }

        // id 로부터 bitmap 생성
        return bitmap;
    }

    /**
     * 밀리초에서 시/분/초를 계산하여 지정된 포맷으로 출력함.
     * -> 0:00 / 1:00 / 10:00 / 1:00:00
     *
     * @param duration
     * @return
     */

    public static String getTime(String duration) {

        long milliSeconds = Long.parseLong(duration);
        int totalSeconds = (int) (milliSeconds / 1000);

        int hour = totalSeconds / 3600;
        int minute = (totalSeconds - (hour * 3600)) / 60;
        int second = (totalSeconds - ((hour * 3600) + (minute * 60)));


        return formattedTime(hour, minute, second);
    }

    /**
     * 계산된 시/분/초 를 지정한 형태의 문자열로 반환함.
     *
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    private static String formattedTime(int hour, int minute, int second) {
        String result = "";

        if (hour > 0) {
            result = hour + ":";
        }

        if (minute >= 10) {
            result = result + minute + ":";
        } else {
            result = result + "0" + minute + ":";
        }

        if (second >= 10) {
            result = result + second;
        } else {
            result = result + "0" + second;
        }

        return result;
    }


}
