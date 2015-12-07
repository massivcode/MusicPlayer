package com.massivcode.androidmusicplayer.events;

import android.os.Parcel;
import android.os.Parcelable;

import com.massivcode.androidmusicplayer.models.MusicInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by massivCode on 2015-11-27.
 */
public class SaveState implements Event, Parcelable {

    private MusicInfo musicInfo;
    private ArrayList<Long> currentPlaylist;
    private int currentPositionAtPlaylist;
    private int currentPlayTime;

    public MusicInfo getMusicInfo() {
        return musicInfo;
    }

    public void setMusicInfo(MusicInfo musicInfo) {
        this.musicInfo = musicInfo;
    }

    public ArrayList<Long> getCurrentPlaylist() {
        return currentPlaylist;
    }

    public void setCurrentPlaylist(ArrayList<Long> currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
    }

    public int getCurrentPositionAtPlaylist() {
        return currentPositionAtPlaylist;
    }

    public void setCurrentPositionAtPlaylist(int currentPositionAtPlaylist) {
        this.currentPositionAtPlaylist = currentPositionAtPlaylist;
    }

    public int getCurrentPlayTime() {
        return currentPlayTime;
    }

    public void setCurrentPlayTime(int currentPlayTime) {
        this.currentPlayTime = currentPlayTime;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.musicInfo, 0);
        dest.writeList(this.currentPlaylist);
        dest.writeInt(this.currentPositionAtPlaylist);
        dest.writeInt(this.currentPlayTime);
    }

    public SaveState() {
    }

    protected SaveState(Parcel in) {
        this.musicInfo = in.readParcelable(MusicInfo.class.getClassLoader());
        this.currentPlaylist = new ArrayList<Long>();
        in.readList(this.currentPlaylist, List.class.getClassLoader());
        this.currentPositionAtPlaylist = in.readInt();
        this.currentPlayTime = in.readInt();
    }

    public static final Creator<SaveState> CREATOR = new Creator<SaveState>() {
        public SaveState createFromParcel(Parcel source) {
            return new SaveState(source);
        }

        public SaveState[] newArray(int size) {
            return new SaveState[size];
        }
    };
}
