package com.massivcode.androidmusicplayer.interfaces;

import com.massivcode.androidmusicplayer.model.MusicInfo;

/**
 * Created by Ray Choe on 2015-11-24.
 */
public class MusicEvent implements Event {

   private MusicInfo mMusicInfo;

    public MusicInfo getMusicInfo() {
        return mMusicInfo;
    }

    public void setMusicInfo(MusicInfo mMusicInfo) {
        this.mMusicInfo = mMusicInfo;
    }
}
