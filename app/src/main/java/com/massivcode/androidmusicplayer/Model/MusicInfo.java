package com.massivcode.androidmusicplayer.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by massivCode on 2015-10-11.
 */
public class MusicInfo implements Parcelable{

    private long _id;
    private Uri uri;
    private String artist;
    private String title;
    private String album;
    private byte[] albumArt;
    private String duration;

    public MusicInfo(long _id, Uri uri, String artist, String title, String album, String duration) {
        this._id = _id;
        this.uri = uri;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.duration = duration;
    }


    public MusicInfo(long _id, Uri uri, String artist, String title, String album, byte[] albumArt, String duration) {
        this._id = _id;
        this.uri = uri;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.albumArt = albumArt;
        this.duration = duration;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public byte[] getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(byte[] albumArt) {
        this.albumArt = albumArt;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeParcelable(this.uri, 0);
        dest.writeString(this.artist);
        dest.writeString(this.title);
        dest.writeString(this.album);
        dest.writeByteArray(this.albumArt);
        dest.writeString(this.duration);
    }

    public MusicInfo() {
    }

    protected MusicInfo(Parcel in) {
        this._id = in.readLong();
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.artist = in.readString();
        this.title = in.readString();
        this.album = in.readString();
        this.albumArt = in.createByteArray();
        this.duration = in.readString();
    }

    public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
        public MusicInfo createFromParcel(Parcel source) {
            return new MusicInfo(source);
        }

        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };
}
