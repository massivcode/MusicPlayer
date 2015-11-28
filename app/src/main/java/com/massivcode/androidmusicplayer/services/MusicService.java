package com.massivcode.androidmusicplayer.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.activities.MainActivity;
import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.interfaces.FinishActivity;
import com.massivcode.androidmusicplayer.interfaces.MusicEvent;
import com.massivcode.androidmusicplayer.interfaces.Playback;
import com.massivcode.androidmusicplayer.interfaces.RequestEvent;
import com.massivcode.androidmusicplayer.interfaces.Restore;
import com.massivcode.androidmusicplayer.interfaces.SaveState;
import com.massivcode.androidmusicplayer.models.MusicInfo;
import com.massivcode.androidmusicplayer.utils.MusicInfoUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

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
    public static final String ACTION_PLAY_SELECTED = "ACTION_PLAY_SELECTED";
    public static final String ACTION_FINISH = "ACTION_FINISH";

    private boolean isReady = false;
    private MediaSessionCompat mSession;
    private MediaMetadataCompat mMetadata;

    private SaveState mSaveState;


    // 수행하는 곳
    private static class UiRefresher extends Handler {
        private final WeakReference<MusicService> mService;

        UiRefresher(MusicService service) {
            mService = new WeakReference<MusicService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mService.get();
            if (service != null) {
                service.sendEvents();
            }
        }
    }


    UiRefresher mHandler = new UiRefresher(this);


    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCompletion(MediaPlayer mp) {

        // 현재 노래 재생이 끝났을 경우 호출되는 리스너

        int lastPosition = mp.getCurrentPosition();
        int duration = mp.getDuration();
//        Log.d(TAG, "더 빨리 호출됨: " + (duration - lastPosition));

        mp.pause();
        mp.reset();

        try {
            if (mCurrentPosition < getCurrentPlaylistSize()) {
                mCurrentPosition += 1;
                mp.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
            } else {
                mp.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(0)));
            }


            mp.prepare();
            isReady = true;
            mp.start();
            // TODO 프래그먼트들에 메세지 보내기
            sendMusicEvent();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            setMetaData();
            showNotification(mMetadata);
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
    private ArrayList<Long> mCurrentPlaylist;
    private int mCurrentPosition;
    private MusicInfo mCurrentMusicInfo;

    private HashMap<Long, MusicInfo> mAllMusicData;

    @Override
    public void onCreate() {
        super.onCreate();

//        Log.d(TAG, "MusicService started");

        // EventBus 등록이 되어서 모든 이벤트를 수신 가능
        EventBus.getDefault().register(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mAllMusicData = MusicInfoUtil.getAllMusicInfo(getApplicationContext());
            }
        }).start();

        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnCompletionListener(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            mAction = intent.getAction();
        }

        if (mAction == null) {
            onDestroy();
            return 0;
        }

        switch (mAction) {
            case ACTION_PLAY_SELECTED:
                mCurrentPosition = intent.getIntExtra("position", 0);
                mCurrentMusicInfo = MusicInfoUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                break;
            case ACTION_PLAY:
                mCurrentPlaylist = (ArrayList<Long>) intent.getSerializableExtra("list");
                mCurrentPosition = intent.getIntExtra("position", 0);
                mCurrentMusicInfo = MusicInfoUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                break;
            case ACTION_PLAY_NEXT:
            case ACTION_PLAY_PREVIOUS:
                mCurrentPosition = intent.getIntExtra("position", 0);
                mCurrentMusicInfo = MusicInfoUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                break;
            case ACTION_FINISH:

                if(mMediaPlayer.isPlaying()) {
                    mHandler.removeMessages(0);
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                } else {
                    mHandler.removeMessages(0);
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                EventBus.getDefault().post(new FinishActivity());
                stopForeground(true);
                stopService(new Intent(getApplicationContext(), MusicService.class));
                break;
        }


        switch (mAction) {

            case ACTION_PLAY_SELECTED:


                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mMediaPlayer.reset();
                    try {

                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;


                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        setMetaData();
                        showNotification(mMetadata);
                    }
                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        setMetaData();
                        showNotification(mMetadata);
                    }
                }
                break;
            case ACTION_PLAY:

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;

                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        setMetaData();
                        showNotification(mMetadata);
                    }
                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        setMetaData();
                        showNotification(mMetadata);
                    }
                }
                break;
            case ACTION_PAUSE:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    sendAllEvent();
                    setMetaData();
                    showNotification(mMetadata);
                } else {
                    mMediaPlayer.start();
                    sendAllEvent();
                    setMetaData();
                    showNotification(mMetadata);
                }
                break;
            case ACTION_PLAY_NEXT:
//                Log.d(TAG, "ACTION_PLAY_NEXT");
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        setMetaData();
                        showNotification(mMetadata);
                    }

                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        setMetaData();
                        showNotification(mMetadata);
                    }
                }

                break;
            case ACTION_PLAY_PREVIOUS:
                if (mMediaPlayer.isPlaying()) {

                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        setMetaData();
                        showNotification(mMetadata);
                    }

                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        setMetaData();
                        showNotification(mMetadata);
                    }
                }
                break;
        }


        return START_STICKY;
    }

    private void setMetaData() {
        // 세션에 메타데이터 셋 (Notification 에서 사용할 것임)
        Bitmap bitmap = MusicInfoUtil.getBitmap(getApplicationContext(), mCurrentMusicInfo.getUri(), 4);
//        Log.d(TAG, "mCurrentMusicInfo.getTitle() : " + mCurrentMusicInfo.getTitle());
        mMetadata = new MediaMetadataCompat.Builder().putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, mCurrentMusicInfo.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mCurrentMusicInfo.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mCurrentMusicInfo.getTitle())
                .build();
        if (mSession == null) {
            mSession = new MediaSessionCompat(this, "tag", null, null);
            mSession.setMetadata(mMetadata);
            mSession.setActive(true);
            mSession.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public void onPlay() {
                    super.onPlay();
                    Toast.makeText(MusicService.this, "onPlay", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        if (event instanceof RequestEvent) {
            sendMusicEvent();
            sendPlayback();
        } else if (event instanceof SaveState) {
            mSaveState = new SaveState();
            MusicInfo musicInfo = getCurrentInfo();
            ArrayList<Long> currentPlaylist = getCurrentPlaylist();
            int currentPlayPosition = getCurrentPosition();
            mSaveState.setMusicInfo(musicInfo);
            mSaveState.setCurrentPlaylist(currentPlaylist);
            mSaveState.setCurrentPositionAtPlaylist(currentPlayPosition);
            mSaveState.setCurrentPlayTime(mMediaPlayer.getCurrentPosition());
        } else if (event instanceof Restore) {
            if (mSaveState != null) {
                EventBus.getDefault().post(mSaveState);
            }
        }
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
        if (mMediaPlayer != null) {
            return mMediaPlayer;
        } else {
            return null;
        }
    }

    public MusicInfo getCurrentInfo() {
        if (mCurrentPlaylist != null) {
//            Log.d(TAG, "case1");
            return MusicInfoUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
        } else {
//            Log.d(TAG, "case2");
            return null;
        }

    }


    public boolean isReady() {
        return isReady;
    }


    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public int getCurrentPlaylistSize() {
        Log.d(TAG, "mCurrentPlaylist.size @ getCurrentPlaylistSize() : " + mCurrentPlaylist.size());
        Log.d(TAG, "getCurrentPosition @ getCurrentPosition() : " + (mCurrentPlaylist.size() - 1));
        return (mCurrentPlaylist.size() - 1);
    }

    private Uri switchIdToUri(long id) {
        return Uri.parse("content://media/external/audio/media/" + id);
    }

    private void sendPlayback() {
        Playback playback = new Playback();
        playback.setCurrentTime(mMediaPlayer.getCurrentPosition());
        playback.setPlaying(mMediaPlayer.isPlaying());
        EventBus.getDefault().post(playback);
    }

    private void sendMusicEvent() {
        MusicEvent musicEvent = new MusicEvent();
        musicEvent.setMediaPlayer(getMediaPlayer());
        musicEvent.setMusicInfo(getCurrentInfo());
        EventBus.getDefault().post(musicEvent);
    }

    private void sendAllEvent() {
        sendMusicEvent();
        mHandler.sendEmptyMessage(0);
    }

    public ArrayList<Long> getCurrentPlaylist() {
        return mCurrentPlaylist;
    }

    public HashMap<Long, MusicInfo> getAllMusicData() {
        return mAllMusicData;
    }

    private void sendEvents() {
        if (mMediaPlayer.isPlaying()) {
//            Log.d(TAG, "UIRefresher is running");
            Playback playback = new Playback();
            playback.setPlaying(mMediaPlayer.isPlaying());
            playback.setCurrentTime(mMediaPlayer.getCurrentPosition());
            EventBus.getDefault().post(playback);

            mHandler.sendEmptyMessageDelayed(0, 1000);

        } else {
//            Log.d(TAG, "UIRefresher is stopped");
            Playback playback = new Playback();
            playback.setPlaying(mMediaPlayer.isPlaying());
            playback.setCurrentTime(mMediaPlayer.getCurrentPosition());
            EventBus.getDefault().post(playback);
        }
    }

    private void showNotification(MediaMetadataCompat metadata) {
        // Notification 작성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        // Hide the timestamp
        builder.setShowWhen(false);
        // Set the Notification style
        builder.setStyle(new NotificationCompat.MediaStyle()
                .setMediaSession(mSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2));
        // Set the Notification color
        builder.setColor(0xFFDB4437);
        // Set the large and small icons
        Bitmap bitmap = MusicInfoUtil.getBitmap(getApplicationContext(), mCurrentMusicInfo.getUri(), 4);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        }
        builder.setLargeIcon(bitmap);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(mCurrentMusicInfo.getTitle());
        builder.setContentText(mCurrentMusicInfo.getArtist());


        int icon;
        if (mMediaPlayer.isPlaying()) {
            Log.d(TAG, "is playing");
            icon = android.R.drawable.ic_media_pause;
        } else {
            Log.d(TAG, "is not playing");
            icon = android.R.drawable.ic_media_play;
        }

        // 이전 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================


        Intent musicPreviousIntent = new Intent(getApplicationContext(), MusicService.class);
        musicPreviousIntent.putExtra("metadata", metadata);
        musicPreviousIntent.setAction(ACTION_PLAY_PREVIOUS);
        musicPreviousIntent.putExtra("position", getPositionAtPreviousOrNext(ACTION_PLAY_PREVIOUS));

        PendingIntent musicPreviousPendingIntent = PendingIntent.getService(getApplicationContext(),
                0, musicPreviousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(android.R.drawable.ic_media_previous, "prev", musicPreviousPendingIntent);
        // =========================================================================================

        // 재생 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================
        Intent musicStopIntent = new Intent(getApplicationContext(), MusicService.class);
        musicStopIntent.putExtra("metadata", metadata);
        musicStopIntent.setAction(ACTION_PAUSE);

        PendingIntent musicStopPendingIntent = PendingIntent.getService(getApplicationContext(),
                1,
                musicStopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(icon, "pause", musicStopPendingIntent);
        // =========================================================================================

        // 다음 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================
        Intent musicNextIntent = new Intent(getApplicationContext(), MusicService.class);
        musicNextIntent.putExtra("metadata", metadata);
        musicNextIntent.setAction(ACTION_PLAY_NEXT);
        musicNextIntent.putExtra("position", getPositionAtPreviousOrNext(ACTION_PLAY_NEXT));

        PendingIntent musicNextPendingIntent = PendingIntent.getService(getApplicationContext(),
                2, musicNextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(android.R.drawable.ic_media_next, "next", musicNextPendingIntent);
        // =========================================================================================

        // 종료 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================
        Intent closeIntent = new Intent(getApplicationContext(), MusicService.class);
        closeIntent.setAction(ACTION_FINISH);

        PendingIntent closePendingIntent = PendingIntent.getService(getApplicationContext(), 3, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(android.R.drawable.arrow_up_float, "close", closePendingIntent);
        // =========================================================================================

        // Notification 터치 했을 때 실행할 PendingIntent 지정
        // =========================================================================================
        Intent activityStartIntent = new Intent(getApplicationContext(), MainActivity.class);
        activityStartIntent.putExtra("restore", getCurrentInfo());
        PendingIntent activityStartPendingIntent = PendingIntent.getActivity(getApplicationContext(),
                1,
                activityStartIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(activityStartPendingIntent);
        // =========================================================================================


        Notification notification = builder.setAutoCancel(true).build();

            startForeground(1, notification);


//        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(1, notification);
//        notification.flags = Notification.Flag
        // Notification 띄우기


//        startForeground(1234, notification);
    }

    public int getPositionAtPreviousOrNext(String flag) {
        int position = getCurrentPosition();
        switch (flag) {
            case ACTION_PLAY_NEXT:
                if (position < getCurrentPlaylistSize()) {
                    position += 1;
                } else {
                    position = 0;
                }
                break;
            case ACTION_PLAY_PREVIOUS:
                if (position > 0) {
                    position -= 1;
                } else {
                    position = getCurrentPlaylistSize();
                }
                break;
        }
        return position;
    }

}
