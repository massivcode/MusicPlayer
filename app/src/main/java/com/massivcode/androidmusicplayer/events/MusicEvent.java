package com.massivcode.androidmusicplayer.events;

import android.media.MediaPlayer;

import com.massivcode.androidmusicplayer.models.MusicInfo;

/**
 * Created by Ray Choe on 2015-11-24.
 */
public class MusicEvent implements Event {

    private MusicInfo mMusicInfo;
    private MediaPlayer mMediaPlayer;

    public MusicInfo getMusicInfo() {
        return mMusicInfo;
    }

    public void setMusicInfo(MusicInfo mMusicInfo) {
        this.mMusicInfo = mMusicInfo;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mMediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
    }
}
