package com.massivcode.androidmusicplayer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.database.MyPlaylistContract;
import com.massivcode.androidmusicplayer.database.MyPlaylistFacade;
import com.massivcode.androidmusicplayer.interfaces.MusicEvent;
import com.massivcode.androidmusicplayer.interfaces.PlayBack;
import com.suwonsmartapp.abl.AsyncBitmapLoader;

/**
 * Created by Ray Choe on 2015-12-02.
 */
public class PlaylistAdapter extends CursorTreeAdapter implements AsyncBitmapLoader.BitmapLoadListener {
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
    }

    public void swapPlayback(PlayBack playback) {
        mPlayback = playback;
    }

    public PlayBack getPlayback() {
        return mPlayback;
    }


    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        return null;
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
        String playlistName = cursor.getString(cursor.getColumnIndexOrThrow(MyPlaylistContract.MyPlaylistEntry.COLUMN_NAME_PLAYLIST));
        int total = mFacade.getSelectedPlaylistTotal(playlistName);
        viewHolder.mGroupPlaylistNameTextView.setText(playlistName);
        viewHolder.mGroupSongsNumberTextView.setText(total + " 곡");
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

        int id = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        viewHolder.mChildArtistTextView.setText(artist);
        viewHolder.mChildTitleTextView.setText(title);
        mAsyncBitmapLoader.loadBitmap(id, viewHolder.mChildAlbumArtImageView);

        if (mMusicEvent != null && mPlayback != null) {

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

        Bitmap bitmap = null;
        if (null != albumArt) {
            bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length, options);
        } else {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
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
