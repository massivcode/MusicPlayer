package com.massivcode.androidmusicplayer.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.model.MusicInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by massivCode on 2015-10-11.
 * <p/>
 * 음원 정보를 얻거나, 현재 재생 중인 음악 목록을 반환하는 것을 도와주는 클래스
 */
public class MusicInfoUtil {

    private static final String TAG = MusicInfoUtil.class.getSimpleName();

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

    /**
     * 해당 포지션의 cursor를 받아서 음원 정보를 얻은 후 반환한다.
     * SongsFragment의 Song ListView를 클릭했을 때 사용한다.
     *
     * @param context : Context
     * @param cursor  : 해당 위치의 음원 정보가 담겨있음.
     * @return MusicInfo
     */
    public static MusicInfo getSelectedMusicInfo(Context context, Cursor cursor) {

        MusicInfo musicInfo;

        // cursor 로부터 정보들을 읽어옴.
        long _id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        Uri uri = Uri.parse("content://media/external/audio/media/" + _id);
        Log.d(TAG, "uri : " + uri);
        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);

        // 앨범 아트 이미지를 byte 배열로 얻어옴.
        byte[] albumArt = retriever.getEmbeddedPicture();

        musicInfo = new MusicInfo(_id, uri, artist, title, album, albumArt, Integer.parseInt(duration));

        return musicInfo;

    }

    /**
     * 서비스에서 해당 곡의 정보를 얻을 때 사용한다.
     * 곡을 재생할 때, 다음/이전 곡으로 넘길 때 마다 사용된다.
     * @param context
     * @param id
     * @return
     */
    public static MusicInfo getSelectedMusicInfo(Context context, long id) {
        MusicInfo musicInfo;
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, new String[]{String.valueOf(id)}, null);

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

        musicInfo = new MusicInfo(_id, uri, artist, title, album, albumArt, Integer.parseInt(duration));

//        private long _id;
//        private Uri uri;
//        private String artist;
//        private String title;
//        private String album;
//        private byte[] albumArt;
//        private int duration;

        return musicInfo;


    }

    /**
     * 단일 재생 시, SongsFragment의 ListView에서 선택한 아이템으로부터 _id 정보를 얻어와 그것을 list에 담아 리턴한다.
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
     * 모두 재생 시, 기기 내에 존재하는 모든 로컬 파일들로부터 _id 정보를 얻어와 그것을 list에 담아 리턴한다.
     * @param context
     * @return
     */
    public static ArrayList<Long> getPlayAllList(Context context) {
        ArrayList<Long> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

        while (cursor.moveToNext()) {
            list.add(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
        }

        cursor.close();

        return list;
    }

    public static Map<Uri, MusicInfo> getAllMusicInfo(Context context) {
        Map<Uri, MusicInfo> map = new HashMap<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

        while (cursor.moveToNext()) {

            long _id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            Uri uri = Uri.parse("content://media/external/audio/media/" + _id);
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            MusicInfo musicInfo = new MusicInfo(_id, uri, artist, title, album, Integer.parseInt(duration));
            map.put(uri, musicInfo);

        }

        cursor.close();

        return map;
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
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
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

    /**
     * MusicInfo 들을 받아서 ArrayList<Uri> 로 반환함.
     *
     * @param infos
     * @return
     */
    public static ArrayList<Uri> makePlaylist(MusicInfo... infos) {
        ArrayList<Uri> uriList = new ArrayList<>();

        for (MusicInfo info : infos) {
            uriList.add(info.getUri());
        }

        return uriList;
    }

    /**
     * MusicInfo가 담긴 Map을 받아서 ArrayList<Uri>로 반환함.
     *
     * @param map
     * @return
     */
    public static ArrayList<Uri> makePlaylist(Map<Uri, MusicInfo> map) {
        ArrayList<Uri> uriList = new ArrayList<>();

        uriList.addAll(map.keySet());

        return uriList;
    }


}
