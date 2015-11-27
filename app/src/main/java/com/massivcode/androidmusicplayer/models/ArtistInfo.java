package com.massivcode.androidmusicplayer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ray Choe on 2015-11-26.
 */
public class ArtistInfo implements Parcelable {

    private long _id;
    private String artist;
    private String total;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "ArtistInfo{" +
                "_id=" + _id +
                ", artist='" + artist + '\'' +
                ", total='" + total + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeString(this.artist);
        dest.writeString(this.total);
    }

    public ArtistInfo() {
    }

    public ArtistInfo(long _id, String artist, String total) {
        this._id = _id;
        this.artist = artist;
        this.total = total;
    }

    protected ArtistInfo(Parcel in) {
        this._id = in.readLong();
        this.artist = in.readString();
        this.total = in.readString();
    }

    public static final Creator<ArtistInfo> CREATOR = new Creator<ArtistInfo>() {
        public ArtistInfo createFromParcel(Parcel source) {
            return new ArtistInfo(source);
        }

        public ArtistInfo[] newArray(int size) {
            return new ArtistInfo[size];
        }
    };
}
