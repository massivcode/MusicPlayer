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

package com.massivcode.androidmusicplayer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.database.MyPlaylistContract;
import com.massivcode.androidmusicplayer.database.MyPlaylistFacade;
import com.massivcode.androidmusicplayer.events.MusicEvent;
import com.massivcode.androidmusicplayer.events.PlayBack;
import com.massivcode.androidmusicplayer.utils.MusicInfoLoadUtil;
import com.suwonsmartapp.abl.AsyncBitmapLoader;


public class PlaylistAdapter extends CursorTreeAdapter implements AsyncBitmapLoader.BitmapLoadListener {
    private static final String TAG = PlaylistAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private Context mContext;
    private AsyncBitmapLoader mAsyncBitmapLoader;
    private MyPlaylistFacade mFacade;

    private MusicEvent mMusicEvent;
    private PlayBack mPlayback;

    public PlaylistAdapter(Cursor cursor, Context context, boolean autoRequery) {
        super(cursor, context, autoRequery);
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mAsyncBitmapLoader = new AsyncBitmapLoader(context);
        mAsyncBitmapLoader.setBitmapLoadListener(this);
        mFacade = new MyPlaylistFacade(context);
    }


    public void swapMusicEvent(MusicEvent musicEvent) {
        mMusicEvent = musicEvent;
        Log.d(TAG, "플레이리스트.스왑 뮤직이벤트");
    }

    public void swapPlayback(PlayBack playback) {
        mPlayback = playback;
        Log.d(TAG, "플레이리스트.스왑 플레이백");
    }

    public PlayBack getPlayback() {
        return mPlayback;
    }


//    public Cursor getChildrenCursor(String playlist_name) {
//        SQLiteDatabase db = mHelper.getReadableDatabase();
//        return db.rawQuery(getChildrenPlaylist_SQL + playlist_name, null);
//    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        return mFacade.getChildrenCursor(groupCursor.getString(groupCursor.getColumnIndexOrThrow(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST)));
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();

        View view = mInflater.inflate(R.layout.item_playlist_group, parent, false);
        viewHolder.mGroupPlaylistNameTextView = (TextView) view.findViewById(R.id.item_playlist_group_name_tv);
        viewHolder.mGroupSongsNumberTextView = (TextView) view.findViewById(R.id.item_playlist_group_total_tv);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String name = cursor.getString(cursor.getColumnIndexOrThrow(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST));
        int total = cursor.getInt(cursor.getColumnIndexOrThrow("music_count"));
        viewHolder.mGroupPlaylistNameTextView.setText(name);
        viewHolder.mGroupSongsNumberTextView.setText(total + mContext.getString(R.string.adapter_songs));
    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();

        View view = mInflater.inflate(R.layout.item_playlist_child, parent, false);

        viewHolder.mChildAlbumArtImageView = (ImageView) view.findViewById(R.id.item_artist_child_album_iv);
        viewHolder.mChildIsPlayingImageView = (ImageView) view.findViewById(R.id.item_songs_isPlay_iv);
        viewHolder.mChildArtistTextView = (TextView) view.findViewById(R.id.item_artist_child_artist_tv);
        viewHolder.mChildTitleTextView = (TextView) view.findViewById(R.id.item_artist_child_title_tv);

        view.setTag(viewHolder);
        return view;
    }


    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_MUSIC_ID));
        String[] info = MusicInfoLoadUtil.getArtistAndTitleFromId(mContext, id);
        String artist = info[0];
        String title = info[1];
        viewHolder.mChildArtistTextView.setText(artist);
        viewHolder.mChildTitleTextView.setText(title);
        mAsyncBitmapLoader.loadBitmap(id, viewHolder.mChildAlbumArtImageView);

        if (mMusicEvent != null && mPlayback != null) {

            if(mMusicEvent.getMusicInfo() != null) {

                if (id == mMusicEvent.getMusicInfo().get_id()) {
                    viewHolder.mChildIsPlayingImageView.setVisibility(View.VISIBLE);

                    if (mPlayback.isPlaying()) {
                        viewHolder.mChildIsPlayingImageView.setSelected(true);
                    } else {
                        viewHolder.mChildIsPlayingImageView.setSelected(false);
                    }

                } else {
                    viewHolder.mChildIsPlayingImageView.setVisibility(View.GONE);
                }
            }

        }
    }

    @Override
    public Bitmap getBitmap(int id) {
        // id 가져오기
        // DB의 _id == id

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mContext, Uri.parse("content://media/external/audio/media/" + id));

        byte[] albumArt = retriever.getEmbeddedPicture();

        // Bitmap 샘플링
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4; // 2의 배수

        Bitmap bitmap;
        if (null != albumArt) {
            bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length, options);
        } else {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_no_image);
        }

        // id 로부터 bitmap 생성
        return bitmap;
    }

    static class ViewHolder {
        TextView mGroupPlaylistNameTextView;
        TextView mGroupSongsNumberTextView;

        ImageView mChildAlbumArtImageView;
        TextView mChildTitleTextView;
        TextView mChildArtistTextView;
        ImageView mChildIsPlayingImageView;
    }
}
