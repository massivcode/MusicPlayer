package com.massivcode.androidmusicplayer.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.interfaces.MusicEvent;
import com.massivcode.androidmusicplayer.model.MusicInfo;
import com.massivcode.androidmusicplayer.util.MusicInfoUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by Ray Choe on 2015-11-24.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = MusicService.class.getSimpleName();

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PLAY_NEXT = "ACTION_PLAY_NEXT";
    public static final String ACTION_PLAY_PREVIOUS = "ACTION_PLAY_PREVIOUS";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";

    private boolean isReady = false;


    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCompletion(MediaPlayer mp) {

        int lastPosition = mp.getCurrentPosition();
        int duration = mp.getDuration();
        Log.d(TAG, "더 빨리 호출됨: " + (duration - lastPosition));

        mp.pause();
        mp.reset();

        try {
            if(mCurrentPosition < getCurrentPlaylistSize()) {
                mCurrentPosition += 1;
                mp.setDataSource(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
            } else {
                mp.setDataSource(getApplicationContext(), mCurrentPlaylist.get(0));
            }


            mp.prepare();
            isReady = true;
            mp.start();
            // TODO 프래그먼트들에 메세지 보내기
            MusicEvent musicEvent = new MusicEvent();
            musicEvent.setMusicInfo(getCurrentInfo());
            EventBus.getDefault().post(musicEvent);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    private MediaPlayer mMediaPlayer;
    private String mAction = null;
    private Map<Uri, MusicInfo> mDataMap;
    private List<Uri> mCurrentPlaylist;
    private int mCurrentPosition;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "MusicService started");

        // EventBus 등록이 되어서 모든 이벤트를 수신 가능
        EventBus.getDefault().register(this);

        mMediaPlayer = new MediaPlayer();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mDataMap = MusicInfoUtil.getAllMusicInfo(getApplicationContext());
            }
        }).start();

        mMediaPlayer.setOnCompletionListener(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mAction = intent.getAction();

        switch (mAction) {
            case ACTION_PLAY:
                mCurrentPlaylist = intent.getParcelableArrayListExtra("list");
                mCurrentPosition = intent.getIntExtra("position", 0);
                break;
            case ACTION_PLAY_NEXT:
            case ACTION_PLAY_PREVIOUS:
                mCurrentPosition = intent.getIntExtra("position", 0);
                break;
        }


        switch (mAction) {
            case ACTION_PLAY:
                if(mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                        mMediaPlayer.prepare();
                        isReady = true;

                        // TODO 프래그먼트들에 메세지 보내기
                        MusicEvent musicEvent = new MusicEvent();
                        musicEvent.setMusicInfo(getCurrentInfo());
                        EventBus.getDefault().post(musicEvent);

                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        MusicEvent musicEvent = new MusicEvent();
                        musicEvent.setMusicInfo(getCurrentInfo());
                        EventBus.getDefault().post(musicEvent);
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case ACTION_PAUSE:
                if(mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.start();
                }
                break;
            case ACTION_PLAY_NEXT:
                if(mMediaPlayer.isPlaying()) {

                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        MusicEvent musicEvent = new MusicEvent();
                        musicEvent.setMusicInfo(getCurrentInfo());
                        EventBus.getDefault().post(musicEvent);
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        MusicEvent musicEvent = new MusicEvent();
                        musicEvent.setMusicInfo(getCurrentInfo());
                        EventBus.getDefault().post(musicEvent);
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case ACTION_PLAY_PREVIOUS:
                if(mMediaPlayer.isPlaying()) {

                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        MusicEvent musicEvent = new MusicEvent();
                        musicEvent.setMusicInfo(getCurrentInfo());
                        EventBus.getDefault().post(musicEvent);
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        MusicEvent musicEvent = new MusicEvent();
                        musicEvent.setMusicInfo(getCurrentInfo());
                        EventBus.getDefault().post(musicEvent);
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }




        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mMediaPlayer = null;

        // 해제 꼭 해주세요
        EventBus.getDefault().unregister(this);

    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {
        MusicEvent musicEvent = (MusicEvent)event;
        Log.d(TAG, "test : " + musicEvent.getMusicInfo().getTitle());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    public MediaPlayer getMediaPlayer() {
        if(mMediaPlayer != null) {
            return mMediaPlayer;
        } else {
            return null;
        }
    }

    public MusicInfo getCurrentInfo() {
        if(mCurrentPlaylist != null) {
            Log.d(TAG, "case1");
            return  mDataMap.get(mCurrentPlaylist.get(mCurrentPosition));
        } else {
            Log.d(TAG, "case2");
            return null;
        }

    }


    public boolean isReady() {
        return isReady;
    }

    public Map<Uri, MusicInfo> getDataMap() {
        return mDataMap;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public int getCurrentPlaylistSize() {
        return mCurrentPlaylist.size()-1;
    }
}
