package com.massivcode.androidmusicplayer.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.model.MusicInfo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by massivCode on 2015-10-11.
 * <p/>
 * 음원 정보를 얻거나, 현재 재생 중인 음악 목록을 반환하는 것을 도와주는 클래스
 *
 *
 *
 * 로컬에 존재하는 모든 음원 가져오는 쿼리 예시.
 * Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

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
    public static String selection_artist = MediaStore.Audio.Media.ARTIST + "=?";

    public static HashMap<Long, MusicInfo> getAllMusicInfo(Context context) {
        HashMap<Long, MusicInfo> map = new HashMap<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);


        while (cursor.moveToNext()) {

            long _id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            Uri uri = Uri.parse("content://media/external/audio/media/" + _id);
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            MusicInfo musicInfo = new MusicInfo(_id, uri, artist, title, album, Integer.parseInt(duration));
            map.put(_id, musicInfo);

        }

        return map;
    }

    public static ArrayList<MusicInfo> switchAllMusicInfoToSelectedMusicInfo(HashMap<Long, MusicInfo> origin, ArrayList<Long> keys) {
        ArrayList<MusicInfo> list = new ArrayList<>();

            for (int i = 0; i < origin.size(); i++) {

                long key;

                if(keys.size() > 1) {
                    key = keys.get(i);
                    if(origin.get(key) != null) {
                        list.add(origin.get(key));
                    }
                } else {
                    key = keys.get(0);
                    if(origin.get(key) != null) {
                        list.add(origin.get(key));
                        break;
                    }
                }



            }

        return list;
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

        cursor.close();

        return musicInfo;


    }

    public static Cursor getArtistInfo(Context context) {
        String[] projection = new String[] {
                MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        };

        return context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, projection, null, null, null);
    }

    public static Cursor getArtistTrackInfo(Context context, String artist) {
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection_artist, new String[]{artist}, null);
    }


//    /**
//     * ArtistAdapter 에서 사용하는 것으로 해당 아티스트의 음원 정보를 리턴한다.
//     * @param context
//     * @param artistList
//     * @return
//     */
//    public static List<List<MusicInfo>> getArtistTrackInfo(Context context, List<ArtistInfo> artistList) {
//        List<List<MusicInfo>> list = new ArrayList<>();
//        List<MusicInfo> childContent = new ArrayList<>();
//
//        for(int i = 0; i < artistList.size(); i++) {
//            String key = artistList.get(i).getArtist();
//            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection_artist, new String[]{key}, null);
//
//            while(cursor.moveToNext()) {
//                long _id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
//                Uri uri = Uri.parse("content://media/external/audio/media/" + _id);
//                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
//                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
//                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
//                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
//                MusicInfo musicInfo = new MusicInfo(_id, uri, artist, title, album, Integer.parseInt(duration));
//                childContent.add(musicInfo);
//            }
//
//            list.add(childContent);
//            childContent = new ArrayList<>();
//            cursor.close();
//        }
//
//        return list;
//    }

    public static ArrayList<MusicInfo> getMusicInfoList(Context context, ArrayList<Long> idList) {
        ArrayList<MusicInfo> musicInfoList = new ArrayList<>();

        for(Long id : idList) {
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

            musicInfoList.add(new MusicInfo(_id, uri, artist, title, album, albumArt, Integer.parseInt(duration)));

            cursor.close();
        }


        return musicInfoList;
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



}
